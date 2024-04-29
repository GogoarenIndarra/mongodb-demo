package pl.jablonski.mongodbdemo.items;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;


@Slf4j
@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    @CrossOrigin
    @PostMapping("/api/items/add-item")
    void addItem(@RequestBody String url) {
        log.info("Request from EXTENSION: {}", url);
        try {
            ItemDto itemDto = createItemBaseOnUrl(url);
            service.createItem(itemDto);
        } catch (Exception e) {
            log.error("Error during creating item: {}", e.getMessage());
            throw new CreatingItemException("Error during creating item");
        }
    }

    public ItemDto createItemBaseOnUrl(final String url) {

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        String html = response.getBody();
        Document doc = Jsoup.parse(html);

        Element titleElement = doc.select("title").first();
        String title = titleElement.text();

        Elements metaElements = doc.select("meta[name=description]");
        String description = metaElements.first().attr("content");

        Elements keywordElements = doc.select("meta[name=keywords]");
        String[] tags = new String[0];
        for (Element keywordElement : keywordElements) {
            tags = keywordElement.attr("content").split(",");
        }

        return ItemDto.builder()
                .shortDescription(title)
                .description(description)
                .link(url)
                .frameworks(String.join(", ", tags))
                .build();
    }
}

class CreatingItemException extends RuntimeException {
    CreatingItemException(String message) {
        super(message);
    }
}

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(CreatingItemException.class)
    public ResponseEntity<ExceptionResponse> restExceptionHandler(final CreatingItemException exception) {
        return new ResponseEntity<>(ExceptionResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

@Builder
@Getter
class ExceptionResponse {
    private int status;
    private String message;
}

