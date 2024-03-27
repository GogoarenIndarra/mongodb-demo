package pl.jablonski.mongodbdemo.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
}
