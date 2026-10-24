package dev.by1337.item.util.text;

import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.decoder.YamlDecoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** Item-specific adapter: EventDSL owns themes and caching, items own display styling. */
@ApiStatus.Internal
public final class RawTextComponentLike implements ComponentLike {
    public static final YamlDecoder<ComponentLike> COMPONENT_DECODER = YamlDecoder.STRING.map(RawTextComponentLike::new);
    private final dev.by1337.edsl.text.RawTextComponentLike text;

    public RawTextComponentLike(String line) {
        text = new dev.by1337.edsl.text.RawTextComponentLike(line);
    }

    public String line() {
        return text.source();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RawTextComponentLike raw && line().equals(raw.line());
    }

    @Override
    public int hashCode() {
        return line().hashCode();
    }

    @Override
    public String toString() {
        return "RawTextComponentLike[line=" + line() + "]";
    }

    public dev.by1337.edsl.text.RawTextComponentLike.Pair asFormated() {
        return text.asFormated(null);
    }

    public Component asComponent(PlaceholderApplier plc, @Nullable Locale locale) {
        Component component = text.asComponent(plc);
        if (locale != null) component = GlobalTranslator.render(component, locale);
        return component.decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public Component asComponent() {
        return text.asComponent().decoration(TextDecoration.ITALIC, false);
    }
}
