package pl.jablonski.mongodbdemo.items;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
class ItemService {

    ItemRepository repository;
    ItemMapper mapper;

    Item createItem(final ItemDto itemDto) {
        return repository.save(mapper.toItem(itemDto));
    }

    void deleteItem(final UUID id) {
        repository.deleteById(id);
        log.info("Item with id: {}, was deleted.", id);
    }

    Item getItemById(final UUID id) {
        return repository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }

    List<Item> showAllItems() {
        return repository.findAll();
    }

    Page<Item> findAllByPage(Pageable pageable) {
        return repository.findAll(pageable);
    }

    List<Item> getItemByName(final String phrase) {
        return repository.findByPhraseInDescription(phrase);
    }

    List<Item> getItemsByCategoryAndSearchPhrase(final Category category, final String phrase) {
        return repository.findByPhraseInDescription(phrase).stream()
                .filter(item -> item.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    List<Item> getItemsByCategory(final Category category) {
        return repository.findByCategory(category);
    }

    Set<String> getAllFrameworks() {
        return repository.findAll().stream()
                .flatMap(item -> item.getFrameworks().stream())
                .collect(Collectors.toSet());
    }

    List<Item> getItemByFramework(String framework) {
        return repository.findByFramework(framework.toLowerCase(Locale.ROOT));
    }

    void update(UUID id, ItemDto itemDto) {
        Item item = repository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
        repository.save(mapper.toItem(itemDto, item.getId()));
    }

    ItemDto mapToDto(Item item) {
        return mapper.toItemDto(item);
    }
}
