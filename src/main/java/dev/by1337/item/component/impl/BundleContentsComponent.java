package dev.by1337.item.component.impl;

import dev.by1337.item.ItemModel;
import dev.by1337.yaml.codec.YamlCodec;

import java.util.List;

public record BundleContentsComponent(List<ItemModel> contents) {
    public static final YamlCodec<BundleContentsComponent> CODEC = YamlCodec.lazyLoad(() ->
            ItemModel.CODEC.listOf().map(
                    BundleContentsComponent::new,
                    contents -> contents.contents
            )
    );
}
