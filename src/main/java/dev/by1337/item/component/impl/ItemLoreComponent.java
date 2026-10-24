package dev.by1337.item.component.impl;

import dev.by1337.edsl.PlayerLocal;
import dev.by1337.edsl.Themes;
import dev.by1337.edsl.util.ByLocaleSelector;
import dev.by1337.item.component.MergeableComponent;
import dev.by1337.item.util.text.RawTextComponentLike;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class ItemLoreComponent implements MergeableComponent<ItemLoreComponent> {
    public static YamlDecoder<ItemLoreComponent> DECODER = RawTextComponentLike.COMPONENT_DECODER.listOf()
            .map(lines -> new ItemLoreComponent(lines));
    @Deprecated
    public static YamlCodec<ItemLoreComponent> CODEC = YamlCodec.of(DECODER);

    private final List<ComponentLike> lore;
    private final boolean hasPlaceholders;
    private final Map<Locale, List<ComponentLike>> variants;
    private final ByLocaleSelector<List<ComponentLike>> selector;

    public ItemLoreComponent(List<ComponentLike> lore) {
        this(lore, Map.of());
    }

    private ItemLoreComponent(List<ComponentLike> lore, Map<Locale, List<ComponentLike>> variants) {
        this.lore = lore;
        this.variants = Map.copyOf(variants);
        selector = new ByLocaleSelector<>(lore);
        variants.forEach(selector::add);
        this.hasPlaceholders = !variants.isEmpty() || lore.stream().anyMatch(v -> v instanceof RawTextComponentLike
                || v instanceof dev.by1337.edsl.text.RawTextComponentLike);
    }

    public static ItemLoreComponent localized(List<ComponentLike> fallback, Map<Locale, List<ComponentLike>> variants) {
        return new ItemLoreComponent(fallback, variants);
    }

    public void forEachLore(@Nullable Locale locale, Consumer<ComponentLike> consumer) {
        if (locale == null) {
            var player = PlayerLocal.get();
            locale = player == null ? Themes.defaultLocale : player.locale();
        }
        selector.select(locale).forEach(consumer);
    }

    public Map<Locale, List<ComponentLike>> variants() {
        return variants;
    }

    public List<ComponentLike> lore(Locale locale) {
        return selector.select(locale);
    }

    public void forEachLore(Consumer<ComponentLike> consumer) {
        for (ComponentLike componentLike : lore) {
            consumer.accept(componentLike);
        }
    }

    public List<ComponentLike> lore() {
        return lore;
    }

    /** Includes theme-dependent text, which must be refreshed even when its current theme is static. */
    public boolean hasPlaceholdersOrLang() {
        return hasPlaceholders;
    }

    @Override
    public ItemLoreComponent and(ItemLoreComponent t1) {
        List<ComponentLike> list = new ArrayList<>(lore);
        list.addAll(t1.lore);
        Map<Locale, List<ComponentLike>> combined = new LinkedHashMap<>();
        var locales = new LinkedHashSet<>(variants.keySet());
        locales.addAll(t1.variants.keySet());
        for (Locale locale : locales) {
            var lines = new ArrayList<>(lore(locale));
            lines.addAll(t1.lore(locale));
            combined.put(locale, lines);
        }
        return new ItemLoreComponent(list, combined);
    }
}
