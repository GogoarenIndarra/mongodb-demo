package pl.jablonski.mongodbdemo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ItemRepository extends MongoRepository<Item, UUID> {

    @Query("{ id: ?0 }")
    Optional<Item> findById(UUID id);

    @Query("{ category: ?0 }")
    List<Item> findByCategory(String category);

    @Query("{ description: { $regex: ?0, $options: 'i' } }")
    List<Item> findByPhraseInDescription(String phrase);

    long count();

}
