package dev.by1337.item;

import dev.by1337.edsl.Themes;
import dev.by1337.item.component.impl.ItemLoreComponent;
import dev.by1337.item.util.text.ItemTextRenderer;
import dev.by1337.item.util.text.RawTextComponentLike;
import dev.by1337.yaml.YamlValue;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ItemTextRendererTest {
    @AfterEach
    void reset() {
        Themes.setSelector(null);
        Themes.setThemeReplacer(null);
    }

    @Test
    void dynamicLoreAppliesThemeBeforeCallerAndSplitsInjectedLines() {
        Themes.setThemeReplacer(id -> s -> s.replace("{border}", "#"));
        var raw = new RawTextComponentLike("{border} {lines}");
        List<Component> output = new ArrayList<>();
        ItemTextRenderer.appendLore(raw, s -> s.replace("{lines}", "first\nsecond"), null, output::add);
        assertEquals(List.of(plain("# first"), plain("second")), output);
    }

    @Test
    void cachedItemDisplayChangesWithThemeAndStillRefreshesLore() {
        AtomicReference<String> selected = new AtomicReference<>("first");
        Themes.setSelector(player -> selected.get());
        Themes.setThemeReplacer(id -> s -> s.replace("{label}", id));
        var lore = ItemLoreComponent.DECODER.decode(YamlValue.wrap(List.of("{label}"))).getOrThrow();
        assertTrue(lore.hasPlaceholdersOrLang());
        var raw = lore.lore().get(0);
        assertEquals(plain("first"), ItemTextRenderer.name(raw, s -> s, null));
        selected.set("second");
        List<Component> output = new ArrayList<>();
        ItemTextRenderer.appendLore(raw, s -> s, null, output::add);
        assertEquals(List.of(plain("second")), output);
    }

    @Test
    void cachedTranslatableIsRenderedPerLocaleWithoutPollutingCache() {
        Translator translator = new Translator() {
            @Override
            public Key name() { return Key.key("test", "item-display"); }

            @Override
            public MessageFormat translate(String key, Locale locale) {
                if (!key.equals("item.label")) return null;
                return new MessageFormat(locale.getLanguage().equals("ru") ? "Russian" : "English", locale);
            }
        };
        GlobalTranslator.get().addSource(translator);
        try {
            var raw = new RawTextComponentLike("<lang:item.label>");
            Component cached = raw.asFormated().component();
            assertNotNull(cached);
            assertEquals(plain("Russian"), ItemTextRenderer.name(raw, s -> s, new Locale("ru", "RU")));
            assertEquals(plain("English"), ItemTextRenderer.name(raw, s -> s, Locale.US));
            List<Component> output = new ArrayList<>();
            ItemTextRenderer.appendLore(raw, s -> s, Locale.US, output::add);
            assertEquals(List.of(plain("English")), output);
            assertSame(cached, raw.asFormated().component());
        } finally {
            GlobalTranslator.get().removeSource(translator);
        }
    }

    private static Component plain(String value) {
        return Component.text(value).decoration(TextDecoration.ITALIC, false);
    }
}
