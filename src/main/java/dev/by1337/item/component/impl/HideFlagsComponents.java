package dev.by1337.item.component.impl;

import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;
import dev.by1337.yaml.decoder.k2v.LookupDecoder;
import org.bukkit.inventory.ItemFlag;

import java.util.HashSet;
import java.util.Set;

public record HideFlagsComponents(Set<ItemFlag> flags) implements MergeableComponent<HideFlagsComponents> {
    public static final YamlDecoder<HideFlagsComponents> DECODER =
            LookupDecoder.fromEnum(ItemFlag.values()).listOf()
                    .map(flags -> new HideFlagsComponents(new HashSet<>(flags)));
    @Deprecated
    public static final YamlCodec<HideFlagsComponents> CODEC = YamlCodec.of(DECODER);

    @Override
    public HideFlagsComponents and(HideFlagsComponents t1) {
        var set = new HashSet<>(flags);
        set.addAll(t1.flags);
        return new HideFlagsComponents(set);
    }
}
