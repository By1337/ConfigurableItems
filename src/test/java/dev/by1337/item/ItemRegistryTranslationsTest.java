package dev.by1337.item;

import dev.by1337.edsl.Themes;
import dev.by1337.item.registry.ItemModelHolder;
import dev.by1337.item.registry.ItemRegistry;
import dev.by1337.item.util.text.RawTextComponentLike;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.decoder.YamlDecoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemRegistryTranslationsTest {
    private static final YamlDecoder<ItemModelHolder> ITEM = ItemModel.DECODER.map(model -> () -> model);

    @AfterEach
    void resetThemes() {
        Themes.setThemeReplacer(null);
    }

    @Test
    void enabledDecoderStoresReferencesAndRegistersOriginalTextWithoutPluginContext() {
        var registry = ItemRegistry.decoder("translated_items", ITEM, true).decode(YamlValue.wrap(Map.of(
                "sword", Map.of("name", "{label}", "lore", List.of("first", "second"))))).getOrThrow();
        var model = registry.byId("sword").itemModel();
        assertEquals("<lang:translated_items.sword.name>", ((RawTextComponentLike) model.get(ItemComponents.NAME)).line());
        assertEquals(Component.translatable("translated_items.sword.name"),
                ((RawTextComponentLike) model.get(ItemComponents.NAME)).asFormated().component());
        var lore = model.get(ItemComponents.LORE).lore();
        assertEquals("<lang:translated_items.sword.lore.0>", ((RawTextComponentLike) lore.get(0)).line());
        assertEquals("<lang:translated_items.sword.lore.1>", ((RawTextComponentLike) lore.get(1)).line());
        Themes.setThemeReplacer(id -> text -> text.replace("{label}", "themed"));
        assertEquals(Component.text("themed"), render("translated_items.sword.name"));
        assertEquals(Component.text("first"), render("translated_items.sword.lore.0"));

        ItemRegistry.decoder("translated_items", ITEM, true).decode(YamlValue.wrap(Map.of(
                "sword", Map.of("lore", List.of("changed"))))).getOrThrow();
        assertEquals(Component.text("changed"), render("translated_items.sword.lore.0"));
        assertEquals(Component.empty(), render("translated_items.sword.lore.1"));
        assertEquals(Component.empty(), render("translated_items.sword.name"));
    }

    @Test
    void originalAndDisabledDecodersLeaveTextUnchanged() {
        var yaml = YamlValue.wrap(Map.of("sword", Map.of("name", "original")));
        for (var decoder : List.of(ItemRegistry.decoder("plain_items", ITEM), ItemRegistry.decoder("plain_items", ITEM, false))) {
            var model = decoder.decode(yaml).getOrThrow().byId("sword").itemModel();
            assertEquals("original", ((RawTextComponentLike) model.get(ItemComponents.NAME)).line());
        }
        assertEquals(Component.translatable("plain_items.sword.name"), render("plain_items.sword.name"));
    }

    @Test
    void failedDecodeDoesNotPublishPartialTranslations() {
        ItemRegistry.decoder("failed_items", ITEM, true).decode(YamlValue.wrap(Map.of(
                "sword", Map.of("name", "kept")))).getOrThrow();
        YamlDecoder<ItemModelHolder> invalid = (ctx, value) ->
                "sword".equals(ctx.getFromKey()) ? DataResult.error("bad item") : ITEM.decode(ctx, value);
        var failed = ItemRegistry.decoder("failed_items", invalid, true).decode(
                YamlValue.wrap(Map.of("sword", Map.of(), "partial", Map.of("name", "unpublished"))));
        assertTrue(failed.hasError());
        assertFalse(failed.hasResult());
        assertEquals(Component.text("kept"), render("failed_items.sword.name"));
        assertEquals(Component.empty(), render("failed_items.partial.name"));
        ItemRegistry.decoder("failed_items", ITEM, true).decode(YamlValue.wrap(Map.of())).getOrThrow();
        assertEquals(Component.empty(), render("failed_items.sword.name"));
    }

    private static Component render(String key) {
        return GlobalTranslator.render(Component.translatable(key), Locale.US);
    }
}
