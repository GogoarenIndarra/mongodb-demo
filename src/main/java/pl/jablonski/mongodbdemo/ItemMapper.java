package pl.jablonski.mongodbdemo;

import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
class ItemMapper {

    Item toItem(final ItemDto dto) throws MalformedURLException {
        return Item.builder()
                .id(UUID.randomUUID())
                .description(dto.getDescription())
                .link(new URL(dto.getLink()))
                .category(Stream.of(dto.getCategory().split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet()))
                .build();
    }
}
