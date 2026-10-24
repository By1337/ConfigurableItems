package dev.by1337.item;

import dev.by1337.edsl.Themes;
import dev.by1337.item.registry.ItemModelHolder;
import dev.by1337.item.registry.ItemRegistry;
import dev.by1337.item.util.text.ItemTextRenderer;
import dev.by1337.item.util.text.RawTextComponentLike;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.decoder.YamlDecoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class LocalizedItemTextTest {
    private static final Locale RU = new Locale("ru", "RU");
    private static final YamlDecoder<ItemModelHolder> ITEM = ItemModel.DECODER.map(model -> () -> model);

    @AfterEach
    void resetThemes() {
        Themes.setSelector(null);
        Themes.setThemeReplacer(null);
    }

    @Test
    void normalRenderSelectsNameAndWholeLoreBeforeThemeAndCallerPlaceholders() {
        var model = ItemModel.DECODER.decode(YamlValue.wrap(Map.of(
                "name", "fallback", "name-ru_RU", "{theme} RU {value}", "name-en_US", "{theme} EN {value}",
                "lore", List.of("fallback lore"), "lore-ru_RU", List.of("RU", "{value}"),
                "lore-en_US", List.of("EN")))).getOrThrow();
        var theme = new AtomicReference<>("first");
        Themes.setSelector(player -> theme.get());
        Themes.setThemeReplacer(id -> text -> text.replace("{theme}", id));
        assertEquals(plain("first RU 42"), name(model, RU));
        assertEquals(plain("first EN 42"), name(model, Locale.US));
        assertEquals(plain("fallback"), name(model, Locale.GERMANY));
        assertEquals(List.of(plain("RU"), plain("42")), lore(model, RU));
        assertEquals(List.of(plain("EN")), lore(model, Locale.US));
        assertEquals(List.of(plain("fallback lore")), lore(model, Locale.GERMANY));
        assertTrue(model.get(ItemComponents.LORE).hasPlaceholdersOrLang());
        theme.set("second");
        assertEquals(plain("second EN 42"), name(model, Locale.US));
        assertEquals(plain("second RU 42"), name(model, RU));
    }

    @Test
    void registryLangReferencesResolveTheSameVariantsAndMissingLocalizedLinesAreEmpty() {
        var registry = ItemRegistry.decoder("localized_items", ITEM, true).decode(YamlValue.wrap(Map.of(
                "sword", Map.of("name", "fallback", "name-ru_RU", "{theme} RU", "name-en_US", "{theme} EN",
                        "lore", List.of("base 0", "base 1"), "lore-en_US", List.of("EN 0"),
                        "lore-ru_RU", List.of("RU 0", "RU 1", "RU 2"))))).getOrThrow();
        var model = registry.byId("sword").itemModel();
        var name = ((RawTextComponentLike) model.get(ItemComponents.NAME)).asFormated().component();
        assertEquals(Component.translatable("localized_items.sword.name"), name);
        Themes.setThemeReplacer(id -> text -> text.replace("{theme}", "theme"));
        assertEquals(Component.text("theme RU"), GlobalTranslator.render(name, RU));
        assertEquals(Component.text("theme EN"), GlobalTranslator.render(name, Locale.US));
        assertEquals(Component.text("fallback"), GlobalTranslator.render(name, Locale.GERMANY));
        assertEquals(3, model.get(ItemComponents.LORE).lore().size());
        assertEquals(Component.text("RU 2"), translate("localized_items.sword.lore.2", RU));
        assertEquals(Component.empty(), translate("localized_items.sword.lore.1", Locale.US));
        assertEquals(Component.empty(), translate("localized_items.sword.lore.2", Locale.GERMANY));
    }

    @Test
    void arbitraryLocalesAndLanguageFallbackWorkWithoutUnlocalizedFields() {
        var model = ItemModel.DECODER.decode(YamlValue.wrap(Map.of(
                "name-fr_FR", "French", "name-ru_RU", "Russian", "name-en_US", "English",
                "lore-fr_FR", List.of("FR"), "lore-ru_RU", List.of("RU"), "lore-en_US", List.of()))).getOrThrow();
        assertEquals(plain("French"), name(model, Locale.FRANCE));
        assertEquals(plain("English"), name(model, new Locale("en", "AU")));
        assertEquals(plain("Russian"), name(model, Locale.GERMANY));
        assertEquals(List.of(), lore(model, Locale.US));
    }

    @Test
    void localizedLoreMergesPerLocale() {
        var first = ItemModel.DECODER.decode(YamlValue.wrap(Map.of(
                "lore", List.of("base"), "lore-en_US", List.of("EN")))).getOrThrow();
        var second = ItemModel.DECODER.decode(YamlValue.wrap(Map.of(
                "lore", List.of("tail"), "lore-ru_RU", List.of("RU tail")))).getOrThrow();
        var merged = first.and(second);
        assertEquals(List.of(plain("EN"), plain("tail")), lore(merged, Locale.US));
        assertEquals(List.of(plain("base"), plain("RU tail")), lore(merged, RU));
    }

    @Test
    void malformedAndDuplicateLocaleSuffixesFailDecode() {
        assertTrue(ItemModel.DECODER.decode(YamlValue.wrap(Map.of("name-ru_???", "bad"))).hasError());
        assertTrue(ItemModel.DECODER.decode(YamlValue.wrap(Map.of(
                "name-ru_RU", "first", "name-ru-RU", "duplicate"))).hasError());
    }

    private static Component name(ItemModel model, Locale locale) {
        return ItemTextRenderer.name(model.get(ItemComponents.NAME), text -> text.replace("{value}", "42"), locale);
    }

    private static List<Component> lore(ItemModel model, Locale locale) {
        var result = new ArrayList<Component>();
        model.get(ItemComponents.LORE).forEachLore(locale, text -> ItemTextRenderer.appendLore(
                text, value -> value.replace("{value}", "42"), locale, result::add));
        return result;
    }

    private static Component plain(String value) {
        return Component.text(value).decoration(TextDecoration.ITALIC, false);
    }

    private static Component translate(String key, Locale locale) {
        return GlobalTranslator.render(Component.translatable(key), locale);
    }
}
