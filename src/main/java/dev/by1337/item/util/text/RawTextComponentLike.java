package dev.by1337.item.util.text;

import dev.by1337.core.util.text.minimessage.BMM;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.decoder.YamlDecoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@ApiStatus.Internal
public record RawTextComponentLike(String line) implements ComponentLike {
    public static final YamlDecoder<ComponentLike> COMPONENT_DECODER = YamlDecoder.STRING.map(s -> {
        if (!hasPlaceholdersOrLang(s)) return BMM.deserialize(s).decoration(TextDecoration.ITALIC, false);
        return new RawTextComponentLike(s);
    });

    public Component asComponent(PlaceholderApplier plc, @Nullable Locale locale) {
        return BMM.deserialize(plc.setPlaceholders(line), locale).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public Component asComponent() {
        return BMM.deserialize(line).decoration(TextDecoration.ITALIC, false);
    }

    private static boolean hasPlaceholdersOrLang(String input) {
        return input.contains("{") || input.contains("%") || input.contains("<lang");
    }
}
