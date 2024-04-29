package pl.jablonski.mongodbdemo.items;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ItemViewControllerTest {

    @Container
    public static final MongoDBContainer container = new MongoDBContainer("mongo:4.0.10")
            .withReuse(true);


    @DynamicPropertySource
    static void mongoDbProperties(final DynamicPropertyRegistry registry) {
        Startables.deepStart(container).join();
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

    @MockBean
    private Clock clock;

    @Autowired
    private ItemService itemService;

    @Autowired
    private WebTestClient webTestClient;

    public static final ZonedDateTime NOW = ZonedDateTime.of(
            LocalDateTime.of(2021, 10, 10, 10, 10),
            ZoneId.of("GMT")
    );

    @BeforeEach
    void setUp() {
        when(clock.getZone()).thenReturn(NOW.getZone());
        when(clock.instant()).thenReturn(NOW.toInstant());
    }

    @Test
    @DisplayName("Should return home page")
    @Order(1)
    void home() {
        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("You need to catch everything. It's like baseball!")));
    }

    @Test
    @DisplayName("Should return create item page")
    @Order(2)
    void creteItemPage() {
        webTestClient.get().uri("/create-item")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertTrue(body.contains("<h2>Create New Item</h2>"));
                    assertTrue(body.contains("<form action=\"/create-item\" method=\"post\">"));
                });
    }

    @Test
    @DisplayName("Should save item and return view item page")
    @Order(3)
    void creteItemReturnItemCreated() {
        final ItemDto itemDto = mockItemDto(Map.of());
        final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("shortDescription", itemDto.getShortDescription());
        formData.add("description", itemDto.getDescription());
        formData.add("link", itemDto.getLink());
        formData.add("frameworks", itemDto.getFrameworks());
        formData.add("category", itemDto.getCategory().toString());
        formData.add("createdAt", itemDto.getCreatedAt().toString());

        AtomicReference<String> redirectLocation = new AtomicReference<>();

        webTestClient.post().uri("/create-item")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", location -> redirectLocation.set(location));

        webTestClient.get().uri(redirectLocation.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertTrue(body.contains(itemDto.getShortDescription()));
                    assertTrue(body.contains(itemDto.getDescription()));
                    assertTrue(body.contains(itemDto.getLink()));
                    assertTrue(body.contains(itemDto.getFrameworks()));
                    assertTrue(body.contains(itemDto.getCategory().toString()));

                });

        final Item itemFromDb = itemService.getItemById(getId(redirectLocation));
        assertEquals("v0 by Vercel", itemFromDb.getShortDescription());
        assertEquals("Generate UI with simple text prompts. Copy, paste, ship.", itemFromDb.getDescription());
        assertEquals("https://v0.dev/", itemFromDb.getLink().toString());
        assertEquals("ui, frontend", itemDto.getFrameworks());
        assertEquals(Category.WEBSITE, itemFromDb.getCategory());
        assertEquals(LocalDateTime.of(2021, 10, 10, 10, 10), itemFromDb.getCreatedAt());
    }

    @Test
    @DisplayName("Should load item return view update-item page")
    @Order(4)
    void updateItem() {
        final var item = itemService.getItemByFramework("ui").get(0);
        final var id = item.getId();

        webTestClient.get().uri("/edit-item/" + item.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertTrue(body.contains("Update existing Item"));
                    assertTrue(body.contains("v0 by Vercel"));
                    assertTrue(body.contains("Generate UI with simple text prompts. Copy, paste, ship."));
                    assertTrue(body.contains("https://v0.dev/"));
                    assertTrue(body.contains("ui, frontend"));
                    assertTrue(body.contains("WEBSITE"));
                });
        final ItemDto itemDto = mockItemDto(Map.of());
        itemDto.setShortDescription("v0 by Vercel - updated");
        itemDto.setDescription("Generate UI with simple text prompts. Copy, paste, ship. - updated");
        itemDto.setFrameworks("ui, frontend, updated");

        final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("shortDescription", itemDto.getShortDescription());
        formData.add("description", itemDto.getDescription());
        formData.add("link", itemDto.getLink());
        formData.add("frameworks", itemDto.getFrameworks());
        formData.add("category", itemDto.getCategory().toString());
        formData.add("createdAt", itemDto.getCreatedAt().toString());

        webTestClient.post().uri("/edit-item/" + id)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is3xxRedirection();


        final Item itemFromDb = itemService.getItemById(id);
        assertEquals("v0 by Vercel - updated", itemFromDb.getShortDescription());
        assertEquals("Generate UI with simple text prompts. Copy, paste, ship. - updated", itemFromDb.getDescription());
        assertEquals("https://v0.dev/", itemFromDb.getLink().toString());
        assertEquals("ui, frontend, updated", itemDto.getFrameworks());
        assertEquals(Category.WEBSITE, itemFromDb.getCategory());
        assertEquals(LocalDateTime.of(2021, 10, 10, 10, 10), itemFromDb.getCreatedAt());
    }

    public static Item mockItem(final Map<String, Object> mockedData) throws URISyntaxException, MalformedURLException {
        return Item.builder()
                .id((UUID) Optional.ofNullable(mockedData.get("id"))
                        .orElse(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")))
                .shortDescription((String) Optional.ofNullable(mockedData.get("shortDescription"))
                        .orElse("Short description"))
                .description((String) Optional.ofNullable(mockedData.get("description"))
                        .orElse("Description"))
                .link((URL) Optional.ofNullable(mockedData.get("link"))
                        .orElse(new URI("https://www.example.com").toURL()))
                .frameworks((Set<String>) Optional.ofNullable(mockedData.get("frameworks"))
                        .orElse(Set.of("java, spring")))
                .category((Category) Optional.ofNullable(mockedData.get("category"))
                        .orElse(Category.ARTICLE))
                .createdAt((LocalDateTime) Optional.ofNullable(mockedData.get("createdAt"))
                        .orElse(LocalDateTime.of(2021, 10, 10, 10, 10)))
                .build();
    }

    public static ItemDto mockItemDto(final Map<String, Object> mockedData) {
        return ItemDto.builder()
                .shortDescription((String) Optional.ofNullable(mockedData.get("shortDescription"))
                        .orElse("v0 by Vercel"))
                .description((String) Optional.ofNullable(mockedData.get("description"))
                        .orElse("Generate UI with simple text prompts. Copy, paste, ship."))
                .link((String) Optional.ofNullable(mockedData.get("link"))
                        .orElse("https://v0.dev/"))
                .frameworks((String) Optional.ofNullable(mockedData.get("frameworks"))
                        .orElse("ui, frontend"))
                .category((Category) Optional.ofNullable(mockedData.get("category"))
                        .orElse(Category.WEBSITE))
                .createdAt((LocalDateTime) Optional.ofNullable(mockedData.get("createdAt"))
                        .orElse(LocalDateTime.of(2021, 10, 10, 10, 10)))
                .build();
    }

    @NotNull
    private static UUID getId(AtomicReference<String> redirectLocation) {
        String uuidString = redirectLocation.toString().substring(redirectLocation.toString().lastIndexOf('/') + 1);
        return UUID.fromString(uuidString);
    }

}