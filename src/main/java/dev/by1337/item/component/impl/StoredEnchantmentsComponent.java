package dev.by1337.item.component.impl;

import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.BukkitYamlDecoders;
import dev.by1337.yaml.decoder.YamlDecoder;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record StoredEnchantmentsComponent(List<Entry> enchantments) implements MergeableComponent<StoredEnchantmentsComponent> {
    public static final YamlDecoder<StoredEnchantmentsComponent> DECODER =
            YamlDecoder.mapOf(BukkitYamlDecoders.enchantment(), YamlDecoder.INT).map(
                    map -> new StoredEnchantmentsComponent(map.entrySet().stream()
                            .map(e -> new Entry(e.getKey(), e.getValue())).toList()));
    @Deprecated
    public static final YamlCodec<StoredEnchantmentsComponent> CODEC = YamlCodec.of(DECODER);

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
