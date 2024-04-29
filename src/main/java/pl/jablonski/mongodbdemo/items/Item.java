package pl.jablonski.mongodbdemo.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Document("items")
@Getter
@AllArgsConstructor
@Builder
class Item {

    @Id
    private UUID id;
    private String shortDescription;
    private String description;
    private URL link;
    private Category category;
    private Set<String> frameworks;
    private LocalDateTime createdAt;
}
