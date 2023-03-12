package pl.jablonski.mongodbdemo.items;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/api/items")
public class ItemController {

    ItemService service;

    @GetMapping("/all")
    List<Item> getAllItems() {
        return service.showAllItems();
    }

    @GetMapping("/category/all")
    Category[] getAllCategory() {
        return Category.values();
    }

    @GetMapping("/frameworks/all")
    Set<String> getAllFrameworks() {
        return service.getAllFrameworks();
    }

    @GetMapping("/name/{phrase}")
    List<Item> getItemByPhase(@PathVariable final String phrase) {
        return service.getItemByName(phrase);
    }

    @GetMapping("/category/{category}")
    List<Item> getItemByCategory(@PathVariable final Category category) {
        return service.getItemsByCategory(category);
    }

    @GetMapping("/frameworks/{framework}")
    List<Item> getItemByFrameworks(@PathVariable final String framework) {
        return service.getItemByFramework(framework);
    }

    @PostMapping
    Item addItem(@RequestBody final ItemDto itemDto) {
        return service.createItem(itemDto);
    }

    @DeleteMapping("/{id}")
    void deleteItem(@PathVariable final UUID id) {
        service.deleteItem(id);
    }
}

