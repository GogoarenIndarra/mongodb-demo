package pl.jablonski.mongodbdemo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
class ItemDto {

    private String description;
    private String link;
    private String category;
}
