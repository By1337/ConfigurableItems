package dev.by1337.item.registry;

import dev.by1337.item.ItemModel;

@FunctionalInterface
public interface ItemModelHolder {
    ItemModel itemModel();
}
