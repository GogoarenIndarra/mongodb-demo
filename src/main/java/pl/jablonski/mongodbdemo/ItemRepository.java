package pl.jablonski.mongodbdemo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface ItemRepository extends MongoRepository<Item, UUID> {

    @Query("{id: '?0'}")
    Optional<Item> findById(UUID id);

    long count();

}
