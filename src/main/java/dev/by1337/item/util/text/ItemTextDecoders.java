package dev.by1337.item.util.text;

import dev.by1337.edsl.Themes;
import dev.by1337.edsl.text.LocalizedText;
import dev.by1337.edsl.util.ByLocaleSelector;
import dev.by1337.edsl.util.Locales;
import dev.by1337.item.component.impl.ItemLoreComponent;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ItemTextDecoders {
    private ItemTextDecoders() {}

    public static final YamlDecoder<ComponentLike> NAME = field("name", RawTextComponentLike.COMPONENT_DECODER)
            .map(values -> values.variants.isEmpty() ? values.base
                    : new LocalizedText(values.fallback(Component.empty()), values.variants));
    public static final YamlDecoder<ItemLoreComponent> LORE = field("lore", RawTextComponentLike.COMPONENT_DECODER.listOf())
            .map(values -> values.base == null && values.variants.isEmpty() ? null
                    : ItemLoreComponent.localized(values.fallback(List.of()), values.variants));

    private static <T> YamlDecoder<Values<T>> field(String name, YamlDecoder<T> decoder) {
        YamlDecoder<Variant<T>> variant = decoder.map((ctx, value) -> new Variant<>(
                Locales.parse(ctx.getFromKey().substring(name.length() + 1)), value));
        var variants = variant.wildcardKey(name + "-*").flatMap(entries -> {
            Map<Locale, T> result = new LinkedHashMap<>();
            for (var entry : entries) {
                if (result.putIfAbsent(entry.locale, entry.value) != null)
                    return DataResult.<Map<Locale, T>>error("Duplicate locale for " + name + ": " + entry.locale);
            }
            return DataResult.success(result);
        });
        return RecordYamlDecoder.mapOf(Values::new, decoder.fieldOf(name), variants.fieldOf());
    }

    private record Variant<T>(Locale locale, T value) {}

    private record Values<T>(T base, Map<Locale, T> variants) {
        T fallback(T empty) {
            if (base != null) return base;
            var selector = new ByLocaleSelector<>(variants.isEmpty() ? empty : variants.values().iterator().next());
            variants.forEach(selector::add);
            return selector.select(Themes.defaultLocale);
        }
    }
}
