package pl.jablonski.mongodbdemo.items;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ItemRepository extends MongoRepository<Item, UUID> {

    @Query("{ id: ?0 }")
    Optional<Item> findById(UUID id);

    @Query("{ category: ?0 }")
    List<Item> findByCategory(Category category);

    @Query("{ frameworks: ?0 }")
    List<Item> findByFramework(String frameworks);

    @Query("{ description: { $regex: ?1, $options: 'i' }, frameworks: ?0 }")
    List<Item> findByPhraseInDescription(String framework, String phrase);


}
