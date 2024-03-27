package pl.jablonski.mongodbdemo.items;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
class ItemMapper {

    Item toItem(final ItemDto dto) {
        try {
            return Item.builder()
                    .id(UUID.randomUUID())
                    .description(dto.getDescription())
                    .shortDescription(dto.getShortDescription())
                    .link(new URL(dto.getLink()))
                    .category(dto.getCategory())
                    .frameworks(Stream.of(dto.getFrameworks().split(","))
                            .map(String::trim)
                            .map(item -> item.toLowerCase(Locale.ROOT))
                            .collect(Collectors.toSet()))
                    .build();
        } catch (MalformedURLException exception) {
            log.error("MalformedURLException occur, exc msg: {}", exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    Item toItem(final ItemDto dto, final UUID id) {
        try {
            return Item.builder()
                    .id(id)
                    .description(dto.getDescription())
                    .shortDescription(dto.getShortDescription())
                    .link(new URL(dto.getLink()))
                    .category(dto.getCategory())
                    .frameworks(Stream.of(dto.getFrameworks().split(","))
                            .map(String::trim)
                            .map(item -> item.toLowerCase(Locale.ROOT))
                            .collect(Collectors.toSet()))
                    .build();
        } catch (MalformedURLException exception) {
            log.error("MalformedURLException occur, exc msg: {}", exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    ItemDto toItemDto(final Item item) {
        return ItemDto.builder()
                .description(item.getDescription())
                .shortDescription(item.getShortDescription())
                .link(item.getLink().toString())
                .category(item.getCategory())
                .frameworks(item.getFrameworks().stream()
                        .collect(Collectors.joining(", ")))
                .build();
    }
}
