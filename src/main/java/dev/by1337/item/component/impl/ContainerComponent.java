package dev.by1337.item.component.impl;

import dev.by1337.item.ItemModel;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public record ContainerComponent(Int2ObjectOpenHashMap<ItemModel> items) {
    public static final YamlDecoder<ContainerComponent> DECODER =
            YamlDecoder.recursive(ignored -> YamlDecoder.mapOf(YamlDecoder.INT, ItemModel.DECODER)
                    .map(map -> new ContainerComponent(new Int2ObjectOpenHashMap<>(map))));
    @Deprecated
    public static final YamlCodec<ContainerComponent> CODEC = YamlCodec.of(DECODER);
}
