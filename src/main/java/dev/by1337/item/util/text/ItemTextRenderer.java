package dev.by1337.item.util.text;

import dev.by1337.core.util.text.minimessage.BMM;
import dev.by1337.edsl.text.LocalizedText;
import dev.by1337.edsl.PlayerLocal;
import dev.by1337.edsl.Themes;
import dev.by1337.plc.PlaceholderApplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;

/** Item display policy, separate from EventDSL's unstyled and untranslated cache. */
@ApiStatus.Internal
public final class ItemTextRenderer {
    private ItemTextRenderer() {}

    public static void appendLore(ComponentLike c, PlaceholderApplier placeholders, @Nullable Locale locale, Consumer<Component> processor) {
        var formatted = formatted(c);
        if (formatted != null) {
            if (formatted.component() != null && !formatted.s().contains("\n")) {
                processor.accept(translateDisplay(formatted.component(), locale));
                return;
            }
            String text = formatted.component() == null
                    ? placeholders.setPlaceholders(formatted.s()) : formatted.s();
            for (String line : text.split("\n")) {
                processor.accept(translateDisplay(BMM.deserialize(line), locale));
            }
        } else {
            processor.accept(translateDisplay(c.asComponent(), locale));
        }
    }

    public static Component name(ComponentLike c, PlaceholderApplier placeholders, @Nullable Locale locale) {
        if (c instanceof LocalizedText localized) {
            var player = PlayerLocal.get();
            Locale selected = locale != null ? locale : player == null ? Themes.defaultLocale : player.locale();
            return name(localized.select(selected), placeholders, locale);
        }
        if (c instanceof RawTextComponentLike raw) {
            return raw.asComponent(placeholders, locale);
        }
        if (c instanceof dev.by1337.edsl.text.RawTextComponentLike raw) {
            return translateDisplay(raw.asComponent(placeholders), locale);
        }
        return translateDisplay(c.asComponent(), locale);
    }

    private static dev.by1337.edsl.text.RawTextComponentLike.@Nullable Pair formatted(ComponentLike text) {
        if (text instanceof RawTextComponentLike raw) return raw.asFormated();
        if (text instanceof dev.by1337.edsl.text.RawTextComponentLike raw) return raw.asFormated(null);
        return null;
    }

    private static Component translateDisplay(Component component, @Nullable Locale locale) {
        if (locale != null) component = GlobalTranslator.render(component, locale);
        return component.decoration(TextDecoration.ITALIC, false);
    }

}
