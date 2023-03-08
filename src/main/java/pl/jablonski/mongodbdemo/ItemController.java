package pl.jablonski.mongodbdemo;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/api/items")
public class ItemController {

    ItemService service;

    @GetMapping("/category/all")
    Set<String> getAllCategory() {
        return service.getAllCategory();
    }

    @GetMapping("/name/{phrase}")
    List<Item> getItemByPhase(@PathVariable final String phrase) {
        return service.getItemByName(phrase);
    }

    @GetMapping("/category/{category}")
    List<Item> getItemByCategory(@PathVariable final String category) {
        return service.getItemsByCategory(category);
    }

    @GetMapping("/all")
    List<Item> getAllItems() {
        return service.showAllItems();
    }

    @PostMapping
    Item addItem(@RequestBody final ItemDto itemDto) throws MalformedURLException {
        log.info("Creating item from dto: {}", itemDto);
        return service.createItem(itemDto);
    }

    @DeleteMapping("/{id}")
    void deleteItem(@PathVariable final UUID id) {
        service.deleteItem(id);
    }
}

