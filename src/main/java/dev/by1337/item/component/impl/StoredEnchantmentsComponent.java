package dev.by1337.item.component.impl;

import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record StoredEnchantmentsComponent(List<Entry> enchantments) implements MergeableComponent<StoredEnchantmentsComponent> {
    public static final YamlCodec<StoredEnchantmentsComponent> CODEC =
            YamlCodec.mapOf(BukkitCodecs.enchantment(), YamlCodec.INT).map(
                            map -> map.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue())).toList(),
                            list -> list.stream().collect(Collectors.toMap(
                                    Entry::enchantment,
                                    Entry::lvl
                            )))
                    .map(StoredEnchantmentsComponent::new, StoredEnchantmentsComponent::enchantments);

    @Override
    public StoredEnchantmentsComponent and(StoredEnchantmentsComponent t1) {
        List<Entry> enchantments = new ArrayList<>(this.enchantments);
        enchantments.addAll(t1.enchantments);
        return new StoredEnchantmentsComponent(enchantments);
    }

    public static StoredEnchantmentsComponent fromMap(Map<Enchantment, Integer> map) {
        return new StoredEnchantmentsComponent(map.entrySet().stream().map(
                e -> new Entry(e.getKey(), e.getValue())
        ).toList());
    }

    public record Entry(Enchantment enchantment, int lvl) {
        public Entry {
            Objects.requireNonNull(enchantment, "enchantment");
        }
    }
}
