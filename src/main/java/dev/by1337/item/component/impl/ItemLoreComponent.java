package dev.by1337.item.component.impl;

import dev.by1337.item.component.MergeableComponent;
import dev.by1337.item.util.text.RawTextComponentLike;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;
import net.kyori.adventure.text.ComponentLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemLoreComponent implements MergeableComponent<ItemLoreComponent> {
    public static YamlDecoder<ItemLoreComponent> DECODER = RawTextComponentLike.COMPONENT_DECODER.listOf()
            .map(ItemLoreComponent::new);
    @Deprecated
    public static YamlCodec<ItemLoreComponent> CODEC = YamlCodec.of(DECODER);

    private final List<ComponentLike> lore;
    private final boolean hasPlaceholders;

    public ItemLoreComponent(List<ComponentLike> lore) {
        this.lore = lore;
        this.hasPlaceholders = lore.stream().anyMatch(v -> v instanceof RawTextComponentLike);
    }

    public void forEachLore(Consumer<ComponentLike> consumer) {
        for (ComponentLike componentLike : lore) {
            consumer.accept(componentLike);
        }
    }

    public List<ComponentLike> lore() {
        return lore;
    }

    public boolean hasPlaceholdersOrLang() {
        return hasPlaceholders;
    }

    @Override
    public ItemLoreComponent and(ItemLoreComponent t1) {
        List<ComponentLike> list = new ArrayList<>(lore);
        list.addAll(t1.lore);
        return new ItemLoreComponent(list);
    }
}
