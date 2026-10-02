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

public record EnchantmentsComponent(List<Entry> enchantments) implements MergeableComponent<EnchantmentsComponent> {
    public static final YamlDecoder<EnchantmentsComponent> DECODER =
            YamlDecoder.mapOf(BukkitYamlDecoders.enchantment(), YamlDecoder.INT).map(
                    map -> new EnchantmentsComponent(map.entrySet().stream()
                            .map(e -> new Entry(e.getKey(), e.getValue())).toList()));
    @Deprecated
    public static final YamlCodec<EnchantmentsComponent> CODEC = YamlCodec.of(DECODER);

    @Override
    public EnchantmentsComponent and(EnchantmentsComponent t1) {
        List<Entry> enchantments = new ArrayList<>(this.enchantments);
        enchantments.addAll(t1.enchantments);
        return new EnchantmentsComponent(enchantments);
    }

    public static EnchantmentsComponent fromMap(Map<Enchantment, Integer> map) {
        return new EnchantmentsComponent(map.entrySet().stream().map(
                e -> new Entry(e.getKey(), e.getValue())
        ).toList());
    }

    public record Entry(Enchantment enchantment, int lvl) {
        public Entry {
            Objects.requireNonNull(enchantment, "enchantment");
        }
    }
}
