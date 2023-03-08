package pl.jablonski.mongodbdemo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URL;
import java.util.Set;
import java.util.UUID;

@Document("items")
@Getter
@AllArgsConstructor
@Builder
class Item {

    @Id
    private UUID id;
    private String description;
    private URL link;
    private Set<String> category;
}
