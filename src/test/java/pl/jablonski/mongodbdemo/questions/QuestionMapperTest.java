package pl.jablonski.mongodbdemo.questions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class QuestionMapperTest {

    private final QuestionMapper questionMapper;

    public QuestionMapperTest() {
        questionMapper = new QuestionMapper();
    }

    @Test
    void testMapToDto() throws MalformedURLException, URISyntaxException {
        // Given
        Question question = mockQuestion(Map.of());
        QuestionDto expectedDto = mockQuestionDto(Map.of());

        // When
        QuestionDto actualDto = questionMapper.mapToDto(question);

        // Then
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void testMapToEntity() throws MalformedURLException, URISyntaxException {
        // Given
        Question expectedEntity = mockQuestion(Map.of());
        QuestionDto questionDto = mockQuestionDto(Map.of());

        // When
        Question actualEntity = questionMapper.mapToEntity(questionDto);

        // Then
        assertEquals(expectedEntity.getQuestion(), actualEntity.getQuestion());
        assertEquals(expectedEntity.getAnswer(), actualEntity.getAnswer());
        assertEquals(expectedEntity.getLink(), actualEntity.getLink());
        assertEquals(expectedEntity.getCategory(), actualEntity.getCategory());
    }

    @Test
    void testMapToEntityWithId() throws MalformedURLException, URISyntaxException {
        // Given
        Question expectedEntity = mockQuestion(Map.of());
        QuestionDto questionDto = mockQuestionDto(Map.of());

        // When
        Question actualEntity = questionMapper.mapToEntity(questionDto, questionDto.getId());

        // Then
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getQuestion(), actualEntity.getQuestion());
        assertEquals(expectedEntity.getAnswer(), actualEntity.getAnswer());
        assertEquals(expectedEntity.getLink(), actualEntity.getLink());
        assertEquals(expectedEntity.getCategory(), actualEntity.getCategory());
    }

    @Test
    void testMapToEntityThrowsException() {
        // Given
        QuestionDto questionDto = mockQuestionDto(Map.of("link", "htttttp://example.com/non-existent-domain"));

        //when
        final var catchException = assertThrows(RuntimeException.class, () ->
                questionMapper.mapToEntity(questionDto));

        //then
        final String expectedExceptionMessage = "unknown protocol:";
        Assertions.assertTrue(catchException.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void testMapToEntityThrowsExceptionv2() {
        // Given
        QuestionDto questionDto = mockQuestionDto(Map.of("link", "http://finance.yahoo.com/q/h?s=^IXIC"));

        //when
        final var catchException = assertThrows(RuntimeException.class, () ->
                questionMapper.mapToEntity(questionDto, questionDto.getId()));

        //then
        final String expectedExceptionMessage = "Illegal character in query at index";
        Assertions.assertTrue(catchException.getMessage().contains(expectedExceptionMessage));
    }


    public static QuestionDto mockQuestionDto(final Map<String, Object> mockedData) {
        return QuestionDto.builder()
                .id((UUID) Optional.ofNullable(mockedData.get("id"))
                        .orElse(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")))
                .question((String) Optional.ofNullable(mockedData.get("question"))
                        .orElse("What is your name?"))
                .answer((String) Optional.ofNullable(mockedData.get("answer"))
                        .orElse("John"))
                .link((String) Optional.ofNullable(mockedData.get("link"))
                        .orElse("https://www.example.com"))
                .category((Category) Optional.ofNullable(mockedData.get("category"))
                        .orElse(Category.JAVA))
                .build();
    }

    public static Question mockQuestion(final Map<String, Object> mockedData) throws URISyntaxException, MalformedURLException {
        return Question.builder()
                .id((UUID) Optional.ofNullable(mockedData.get("id"))
                        .orElse(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")))
                .question((String) Optional.ofNullable(mockedData.get("question"))
                        .orElse("What is your name?"))
                .answer((String) Optional.ofNullable(mockedData.get("answer"))
                        .orElse("John"))
                .link((URL) Optional.ofNullable(mockedData.get("link"))
                        .orElse(new URI("https://www.example.com").toURL()))
                .category((Category) Optional.ofNullable(mockedData.get("category"))
                        .orElse(Category.JAVA))
                .build();
    }
}