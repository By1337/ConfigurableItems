package dev.by1337.item.component.impl;

import dev.by1337.item.ItemModel;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;

import java.util.List;

public record BundleContentsComponent(List<ItemModel> contents) {
    public static final YamlDecoder<BundleContentsComponent> DECODER =
            YamlDecoder.recursive(ignored -> ItemModel.DECODER.listOf().map(BundleContentsComponent::new));
    @Deprecated
    public static final YamlCodec<BundleContentsComponent> CODEC = YamlCodec.of(DECODER);
}
