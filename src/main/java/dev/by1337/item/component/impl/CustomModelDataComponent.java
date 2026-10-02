package dev.by1337.item.component.impl;

import dev.by1337.item.util.ColorHolder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import org.bukkit.Color;

import java.util.List;

public record CustomModelDataComponent(List<Float> floats, List<Boolean> flags, List<String> strings,
                                       List<Color> colors) {
    public static YamlDecoder<CustomModelDataComponent> DECODER = RecordYamlDecoder.mapOf(
            CustomModelDataComponent::new,
            YamlDecoder.FLOAT.listOf().fieldOf("floats", List.of()),
            YamlDecoder.BOOL.listOf().fieldOf("flags", List.of()),
            YamlDecoder.STRING.listOf().fieldOf("strings", List.of()),
            ColorHolder.DECODER.map(ColorHolder::toBukkit).listOf()
                    .fieldOf("colors", List.of())
    ).whenPrimitive(YamlDecoder.FLOAT.map(
            //Deprecated 1.21.5
            i -> new CustomModelDataComponent(List.of(i), List.of(), List.of(), List.of())
    ));
    @Deprecated
    public static YamlCodec<CustomModelDataComponent> CODEC = YamlCodec.of(DECODER);
}
