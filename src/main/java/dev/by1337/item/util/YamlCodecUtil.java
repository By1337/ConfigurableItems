package dev.by1337.item.util;

import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import org.jetbrains.annotations.NotNull;

public class YamlCodecUtil {
    public static <T> YamlCodec<T> anyOf(YamlCodec<T> first, YamlCodec<T> second) {
        return new YamlCodec<T>() {
            @Override
            public DataResult<T> decode(YamlValue yamlValue) {
                var v = first.decode(yamlValue);
                if (v.hasResult()) return v;
                return second.decode(yamlValue);
            }

            @Override
            public YamlValue encode(T t) {
                return first.encode(t);
            }

            @Override
            public @NotNull SchemaType schema() {
                return first.schema();
            }
        };
    }
}
