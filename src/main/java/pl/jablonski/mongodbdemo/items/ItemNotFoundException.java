package pl.jablonski.mongodbdemo.items;

import java.util.UUID;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(UUID id) {
        super("Item with id: " + id + " not found.");
    }
}
