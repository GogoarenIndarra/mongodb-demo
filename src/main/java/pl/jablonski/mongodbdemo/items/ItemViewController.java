package pl.jablonski.mongodbdemo.items;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ItemViewController {

    ItemService service;

    @GetMapping({"/", "home"})
    String home(Model model) {
        Set<String> frameworks = service.getAllFrameworks();
        model.addAttribute("frameworks", frameworks);
        return "home-page";
    }

    @GetMapping("/create-item")
    String addItem(Model model) {
        model.addAttribute("itemDto", new ItemDto());
        return "items/create-item";
    }

    @PostMapping("/create-item")
    String addItem(@Validated @ModelAttribute("itemDto") ItemDto itemDto, BindingResult result) {

        if (result.hasErrors()) {
            return "items/create-item";
        }
        var item = service.createItem(itemDto);
        return "redirect:/items/%s".formatted(item.getId().toString());
    }

    @GetMapping("/items/{id}")
    String getItem(@PathVariable("id") String itemId, Model model) {
        var item = service.getItemById(UUID.fromString(itemId));
        model.addAttribute("shortDescription", item.getShortDescription());
        model.addAttribute("description", item.getDescription());
        model.addAttribute("link", item.getLink());
        model.addAttribute("category", item.getCategory());
        model.addAttribute("frameworks", String.join(", ", item.getFrameworks()));
        return "items/view-item";
    }

    @GetMapping("/search-item")
    String searchItem(@RequestParam String framework, Model model) {
        log.info("Search by Framework: {}", framework);
        var items = service.getItemByFramework(framework.toLowerCase());
        model.addAttribute("items", items);
        return "items/view-all-items-np";
    }


    @GetMapping("/view-all-items")
    String getAllItemsWithPage(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int sizePerPage) {

        Pageable pageable = PageRequest.of(page, sizePerPage, Sort.Direction.DESC, "createdAt");
        Page<Item> itemPage = service.findAllByPage(pageable);
        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("pageInfo", itemPage);

        return "items/view-all-items";
    }

    @GetMapping("/delete-item/{id}")
    String deleteItem(@PathVariable("id") String itemId) {
        service.deleteItem(UUID.fromString(itemId));
        return "home-page";
    }

    @GetMapping("/edit-item/{id}")
    String updateItem(@PathVariable("id") String itemId, Model model) {
        Item item = service.getItemById(UUID.fromString(itemId));
        ItemDto itemDto = service.mapToDto(item);
        model.addAttribute("itemDto", itemDto);
        return "items/update-item";
    }

    @PostMapping("/edit-item/{id}")
    String updateItem(@PathVariable("id") String itemId,
                      @Validated @ModelAttribute("itemDto") ItemDto itemDto,
                      BindingResult result) {

        if (result.hasErrors()) {
            return "items/update-item";
        }
        service.update(UUID.fromString(itemId), itemDto);
        return "redirect:/items/%s".formatted(itemId);
    }

}
