package pl.jablonski.mongodbdemo;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
class ItemService {

    ItemRepository repository;
    ItemMapper mapper;

    Item createItem(final ItemDto itemDto) throws MalformedURLException {
        return repository.save(mapper.toItem(itemDto));
    }

    void deleteItem(final UUID id) {
        repository.deleteById(id);
        log.info("Item with id: {}, was deleted.", id);
    }

    List<Item> showAllItems() {
        return repository.findAll();
    }

    List<Item> getItemByName(final String phrase) {
        return repository.findByPhraseInDescription(phrase);
    }

    List<Item> getItemsByCategory(final String category) {
        return repository.findByCategory(category);
    }

    long findCountOfItems() {
        return repository.count();
    }

    public Set<String> getAllCategory() {
        Set<String> response = new HashSet<>();
        repository.findAll().forEach(item -> response.addAll(item.getCategory()));
        return response;
    }
}
