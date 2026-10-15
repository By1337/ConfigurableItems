package dev.by1337.item.component;

import dev.by1337.yaml.decoder.YamlDecoder;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record BaseComponent<T>(int id, String name, YamlDecoder<T> decoder) {
}
