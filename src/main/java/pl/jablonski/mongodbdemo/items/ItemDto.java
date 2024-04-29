package pl.jablonski.mongodbdemo.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
class ItemDto {

    private String shortDescription;
    private String description;
    private String link;
    private Category category;
    private String frameworks;
    private LocalDateTime createdAt;
}
