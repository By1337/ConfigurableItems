package dev.by1337.item.component.impl;

import dev.by1337.core.bridge.registry.LegacyRegistryBridge;
import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.InlineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record PotionContentsComponent(
        List<PotionEffect> contents) implements MergeableComponent<PotionContentsComponent> {


    public static final YamlCodec<PotionContentsComponent> CODEC =
            Codecs.POTION_EFFECT_LIST_CODEC.map(
                    PotionContentsComponent::new,
                    PotionContentsComponent::contents
            );

    @Override
    public PotionContentsComponent and(PotionContentsComponent t1) {
        List<PotionEffect> list = new ArrayList<>(contents);
        list.addAll(t1.contents);
        return new PotionContentsComponent(list);
    }

    private static class Codecs {
        private static final YamlCodec<int[]> TWO_INTS = YamlCodec.STRING.flatMap(
                s -> {
                    String[] split = s.split("\\s+", 2);
                    if (split.length != 2) return DataResult.error("expected '<number> <number>', but got '{}'", s);
                    return YamlCodec.INT.decode(split[0]).flatMap(i1 ->
                            YamlCodec.INT.decode(split[1]).mapValue(i2 -> new int[]{i1, i2})
                    );

                },
                arr -> getByIndex(arr, 0) + " " + getByIndex(arr, 1)
        );

        private static int getByIndex(int[] arr, int index) {
            return arr.length > index ? arr[index] : 0;
        }

        private static final YamlCodec<List<PotionEffect>> POTION_EFFECT_LIST_CODEC =
                YamlCodec.mapOf(LegacyRegistryBridge.MOB_EFFECT.yamlCodec(), TWO_INTS).map(
                        map -> map.entrySet().stream().map(e -> new PotionEffect(e.getKey(), e.getValue()[0], e.getValue()[1])).toList(),
                        list -> list.stream().collect(Collectors.toMap(
                                PotionEffect::getType,
                                v -> new int[]{v.getDuration(), v.getAmplifier()}
                        ))
                );

    }
}
