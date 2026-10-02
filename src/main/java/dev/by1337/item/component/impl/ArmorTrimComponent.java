package dev.by1337.item.component.impl;

import dev.by1337.core.ServerVersion;
import dev.by1337.item.util.Holder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.KeyedYamlDecoder;
import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("all")
public record ArmorTrimComponent(Holder<ArmorTrim> armorTrim) {
    @Deprecated
    public static final @Nullable YamlCodec<ArmorTrimComponent> CODEC;
    public static final @Nullable YamlDecoder<ArmorTrimComponent> DECODER;

    public ArmorTrimComponent(Holder<TrimMaterial> material, Holder<TrimPattern> pattern) {
        this(new Holder<>(new ArmorTrim(material.get(), pattern.get())));
    }

    public boolean has() {
        return armorTrim.has();
    }

    static {
        if (ServerVersion.is1_19_4orNewer()) {
            DECODER = RecordYamlDecoder.mapOf(
                    ArmorTrimComponent::new,
                    new KeyedYamlDecoder<>(Registry.TRIM_MATERIAL, "trim_material")
                            .map(Holder::new).fieldOf("material"),
                    new KeyedYamlDecoder<>(Registry.TRIM_PATTERN, "trim_pattern")
                            .map(Holder::new).fieldOf("pattern")
            );
            CODEC = YamlCodec.of(DECODER);
        } else {
            DECODER = null;
            CODEC = null;
        }
    }
}
