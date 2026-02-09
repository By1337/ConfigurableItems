package dev.by1337.item.component.impl;

import dev.by1337.core.util.text.component.SourcedComponentLike;
import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.codec.YamlCodec;
import net.kyori.adventure.text.ComponentLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemLoreComponent implements MergeableComponent<ItemLoreComponent> {
    public static YamlCodec<ItemLoreComponent> CODEC = SourcedComponentLike.COMPONENT_LIKE_CODEC
            .listOf()
            .map(ItemLoreComponent::new, ItemLoreComponent::lore);

    private final List<ComponentLike> lore;
    private final boolean hasPlaceholders;

    public ItemLoreComponent(List<ComponentLike> lore) {
        this.lore = lore;
        boolean mutable = false;
        for (ComponentLike like : lore) {
            if (like instanceof SourcedComponentLike s){
                if (hasPlaceholders(s.source())){
                    mutable = true;
                    break;
                }
            }
        }
        this.hasPlaceholders = mutable;
    }

    public void forEachLore(Consumer<ComponentLike> consumer) {
        for (ComponentLike componentLike : lore) {
            consumer.accept(componentLike);
        }
    }

    public List<ComponentLike> lore() {
        return lore;
    }

    public boolean hasPlaceholders() {
        return hasPlaceholders;
    }

    private static boolean hasPlaceholders(String input) {
        return input.contains("{") || input.contains("%");
    }


    @Override
    public ItemLoreComponent and(ItemLoreComponent t1) {
        List<ComponentLike> list = new ArrayList<>(lore);
        list.addAll(t1.lore);
        return new ItemLoreComponent(list);
    }
}
