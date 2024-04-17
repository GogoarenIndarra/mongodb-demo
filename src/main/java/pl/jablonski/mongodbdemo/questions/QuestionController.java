package pl.jablonski.mongodbdemo.questions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Controller
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/create-question")
    String addQuestion(Model model) {
        model.addAttribute("itemDto", new QuestionDto());
        return "questions/create-question";
    }

    @PostMapping("/create-question")
    String addQuestion(@Validated @ModelAttribute("questionDto") QuestionDto questionDto, BindingResult result) {

        if (result.hasErrors()) {
            return "questions/create-question";
        }
        var question = questionService.createQuestion(questionDto);
        return "redirect:/questions/%s".formatted(question.getId().toString());
    }

    @GetMapping("/questions/{id}")
    String getQuestion(@PathVariable("id") String id, Model model) {
        var item = questionService.getQuestionById(UUID.fromString(id));
        model.addAttribute("question", item.getQuestion());
        model.addAttribute("answer", item.getAnswer());
        model.addAttribute("category", item.getCategory());
        model.addAttribute("link", item.getLink());
        return "questions/view-question";
    }

    @GetMapping("/view-all-questions")
    String getAllQuestionsWithPage(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "5") int sizePerPage) {

        Pageable pageable = PageRequest.of(page, sizePerPage, Sort.Direction.DESC, "category");
        Page<Question> questionPage = questionService.findAllByPage(pageable);
        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("pageInfo", questionPage);

        return "questions/view-all-questions";
    }

    @GetMapping("/delete-question/{id}")
    String deleteQuestion(Model model, @PathVariable("id") String questionId) {
        questionService.deleteQuestion(UUID.fromString(questionId));
        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "category");
        Page<Question> questionPage = questionService.findAllByPage(pageable);
        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("pageInfo", questionPage);
        return "questions/view-all-questions";
    }

    @GetMapping("/edit-question/{id}")
    String updateQuestion(@PathVariable("id") String questionId, Model model) {
        Question question = questionService.getQuestionById(UUID.fromString(questionId));
        QuestionDto questionDto = questionService.mapToDto(question);
        model.addAttribute("questionDto", questionDto);
        return "questions/update-question";
    }

    @PostMapping("/edit-question/{id}")
    String updateQuestion(@PathVariable("id") String questionId,
                          @Validated @ModelAttribute("questionDto") QuestionDto questionDto,
                          BindingResult result) {

        if (result.hasErrors()) {
            return "questions/update-question";
        }
        questionService.update(UUID.fromString(questionId), questionDto);
        return "redirect:/questions/%s".formatted(questionId);
    }

    @GetMapping("/search-questions")
    String searchQuestion(@RequestParam Category category, Model model) {
        var questions = questionService.getByCategory(category);
        model.addAttribute("questions", questions);
        return "questions/view-all-questions-np";
    }
}

@Service
@Slf4j
class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
    }

    public Question createQuestion(QuestionDto questionDto) {
        var question = questionMapper.mapToEntity(questionDto);
        return questionRepository.save(question);
    }

    public Question getQuestionById(UUID id) {
        return questionRepository.findById(id).orElseThrow(() -> new QuestionNotFoundException(id));
    }

    public Page<Question> findAllByPage(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    public void deleteQuestion(UUID questionId) {
        questionRepository.deleteById(questionId);
        log.info("Question with id: {}, was deleted.", questionId);
    }

    public void update(UUID uuid, QuestionDto questionDto) {
        Question question = questionRepository.findById(uuid).orElseThrow(() -> new QuestionNotFoundException(uuid));
        questionRepository.save(questionMapper.mapToEntity(questionDto, question.getId()));
    }

    public QuestionDto mapToDto(Question question) {
        return questionMapper.mapToDto(question);
    }

    public List<Question> getByCategory(Category category) {
        return questionRepository.findByCategory(category);
    }
}

@Repository
interface QuestionRepository extends MongoRepository<Question, UUID> {

    @Query("{ category: ?0 }")
    List<Question> findByCategory(Category category);
}

@Document("questions")
@Getter
@AllArgsConstructor
@Builder
class Question {

    @Id
    private UUID id;
    private String question;
    private String answer;
    private URL link;
    private Category category;
}

enum Category {
    JAVA,
    SPRING,
    SQL,
    ALGORITHMS,
    DESIGN_PATTERNS,
    TESTING,
    MICROSERVICES,
    KAFKA,
    DOCKER,
    OTHER
}

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
class QuestionDto {

    private UUID id;
    private String question;
    private String answer;
    private String link;
    private Category category;

}

@Slf4j
@Component
class QuestionMapper {

    QuestionDto mapToDto(Question question) {
        return QuestionDto.builder()
                .id(question.getId())
                .question(question.getQuestion())
                .answer(question.getAnswer())
                .link(String.valueOf(question.getLink()))
                .category(question.getCategory())
                .build();
    }

    Question mapToEntity(QuestionDto questionDto) {
        try {
            return Question.builder()
                    .id(UUID.randomUUID())
                    .question(questionDto.getQuestion())
                    .answer(questionDto.getAnswer())
                    .link(new URI(questionDto.getLink()).toURL())
                    .category(questionDto.getCategory())
                    .build();
        } catch (MalformedURLException | URISyntaxException exception) {
            log.error("MalformedURLException | URISyntaxException occur, exc msg: {}", exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    Question mapToEntity(QuestionDto questionDto, UUID id) {
        try {
            return Question.builder()
                    .id(id)
                    .question(questionDto.getQuestion())
                    .answer(questionDto.getAnswer())
                    .link(new URI(questionDto.getLink()).toURL())
                    .category(questionDto.getCategory())
                    .build();
        } catch (MalformedURLException | URISyntaxException exception) {
            log.error("MalformedURLException | URISyntaxException occur, exc msg: {}", exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }
}

class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(UUID id) {
        super("Question with id: " + id + " not found.");
    }
}

