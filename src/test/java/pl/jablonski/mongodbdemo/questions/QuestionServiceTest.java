package pl.jablonski.mongodbdemo.questions;

import org.instancio.Instancio;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuestionServiceTest {

    @Container
    public static final MongoDBContainer container = new MongoDBContainer("mongo:4.0.10")
            .withReuse(true);


    @DynamicPropertySource
    static void mongoDbProperties(final DynamicPropertyRegistry registry) {
        Startables.deepStart(container).join();
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

    @Autowired
    private QuestionRepository repository;

    @Autowired
    private QuestionService service;

    @Test
    @Order(1)
    void createQuestion() {
        //given
        final QuestionDto questionDto = getQuestionDto();

        //when
        service.createQuestion(questionDto);

        //then
        var fromRepo = repository.findAll();
        assertEquals(1, fromRepo.size());
        assertEquals(questionDto.getQuestion(), fromRepo.getFirst().getQuestion());
        assertEquals(questionDto.getAnswer(), fromRepo.getFirst().getAnswer());
        assertEquals(questionDto.getLink(), fromRepo.getFirst().getLink().toString());
        assertEquals(questionDto.getCategory(), fromRepo.getFirst().getCategory());
        assertNotNull(fromRepo.getFirst().getId());
    }

    @Test
    @Order(2)
    void getQuestionById() {
        final QuestionDto questionDto = getQuestionDto();
        var id = service.createQuestion(questionDto).getId();
        var question = service.getQuestionById(id);

        assertEquals(questionDto.getQuestion(), question.getQuestion());
        assertEquals(questionDto.getAnswer(), question.getAnswer());
        assertEquals(questionDto.getLink(), question.getLink().toString());
        assertEquals(questionDto.getCategory(), question.getCategory());
    }

    @Test
    @Order(3)
    void getByCategory() {
        var fromRepo = repository.findAll();
        final Category category = fromRepo.getFirst().getCategory();
        var results = service.getByCategory(category);
        assertEquals(category, results.get(0).getCategory());
    }

    @Test
    @Order(4)
    public void testFindAllByPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);

        // When
        Page<Question> actualPage = service.findAllByPage(pageable);

        // Then
        assertEquals(0, actualPage.getNumber());
        assertEquals(5, actualPage.getSize());
        assertEquals(1, actualPage.getTotalPages());
    }

    @Test
    @Order(5)
    void update() {
        final QuestionDto questionDto = getQuestionDto();
        var id = service.createQuestion(questionDto).getId();
        questionDto.setQuestion("new question");
        service.update(id, questionDto);
        var question = service.getQuestionById(id);
        assertEquals("new question", question.getQuestion());
    }

    @Test
    @Order(6)
    void deleteQuestion() {
        final QuestionDto questionDto = getQuestionDto();
        var id = service.createQuestion(questionDto).getId();
        service.deleteQuestion(id);
        assertThrows(QuestionNotFoundException.class, () -> service.getQuestionById(id));
    }

    @Test
    @Order(7)
    void getQuestionById_throwsException() {
        final QuestionDto questionDto = getQuestionDto();
        var id = service.createQuestion(questionDto).getId();
        service.deleteQuestion(id);
        assertThrows(QuestionNotFoundException.class, () -> service.getQuestionById(id));
    }

    @Test
    @Order(8)
    void update_throwsException() {
        final QuestionDto questionDto = getQuestionDto();
        var id = service.createQuestion(questionDto).getId();
        questionDto.setQuestion("new question");
        service.deleteQuestion(id);
        assertThrows(QuestionNotFoundException.class, () -> service.update(id, questionDto));
    }

    @Test
    @Order(9)
    void mapToDto() throws MalformedURLException, URISyntaxException {
        final Question question = new Question(
                UUID.randomUUID(),
                "question",
                "answer",
                new URI("https://www.example.com").toURL(),
                Category.TESTING);
        final QuestionDto questionDto = service.mapToDto(question);
        assertEquals(question.getId(), questionDto.getId());
        assertEquals(question.getQuestion(), questionDto.getQuestion());
        assertEquals(question.getAnswer(), questionDto.getAnswer());
        assertEquals(question.getLink().toString(), questionDto.getLink());
        assertEquals(question.getCategory(), questionDto.getCategory());
    }

    private static QuestionDto getQuestionDto() {
        return Instancio.of(QuestionDto.class)
                .ignore(field(QuestionDto::getId))
                .generate(field(QuestionDto::getLink), gen -> gen.text().pattern("https://www.#a#a#a#a#a#a#a#a#a#a#a#a.com"))
                .create();
    }
}