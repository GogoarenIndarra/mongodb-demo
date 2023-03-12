package pl.jablonski.mongodbdemo.items;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
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
                    .link(new URL(dto.getLink()))
                    .category(dto.getCategory())
                    .frameworks(Stream.of(dto.getFrameworks().split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet()))
                    .build();
        } catch (MalformedURLException exception) {
            log.error("MalformedURLException occur, exc msg: {}", exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }
}
