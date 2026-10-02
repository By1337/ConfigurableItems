package dev.by1337.item.component.impl;

import dev.by1337.core.bridge.registry.LegacyRegistryBridge;
import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public record PotionContentsComponent(
        List<PotionEffect> contents) implements MergeableComponent<PotionContentsComponent> {


    public static final YamlDecoder<PotionContentsComponent> DECODER =
            Decoders.POTION_EFFECT_LIST_DECODER.map(PotionContentsComponent::new);
    @Deprecated
    public static final YamlCodec<PotionContentsComponent> CODEC = YamlCodec.of(DECODER);

    @Override
    public PotionContentsComponent and(PotionContentsComponent t1) {
        List<PotionEffect> list = new ArrayList<>(contents);
        list.addAll(t1.contents);
        return new PotionContentsComponent(list);
    }

    private static class Decoders {
        private static final YamlDecoder<int[]> TWO_INTS = YamlDecoder.STRING.flatMap(
                s -> {
                    String[] split = s.split("\\s+", 2);
                    if (split.length != 2) return DataResult.error("expected '<number> <number>', but got '{}'", s);
                    return YamlDecoder.INT.decode(YamlValue.wrap(split[0])).flatMap(i1 ->
                            YamlDecoder.INT.decode(YamlValue.wrap(split[1])).map(i2 -> new int[]{i1, i2})
                    );

                }
        );

        private static final YamlDecoder<List<PotionEffect>> POTION_EFFECT_LIST_DECODER =
                YamlDecoder.mapOf(LegacyRegistryBridge.MOB_EFFECT.yamlCodec().asDecoder(), TWO_INTS)
                        .map(map -> map.entrySet().stream()
                                .map(e -> new PotionEffect(e.getKey(), e.getValue()[0], e.getValue()[1])).toList());

    }
}
