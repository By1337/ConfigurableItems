package dev.by1337.item.util.text;

import dev.by1337.core.util.text.minimessage.BMM;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;

public class LazyDecodeComponentLike implements ComponentLike {
    private final String line;
    private Component component;

    public LazyDecodeComponentLike(String line) {
        this.line = line;
    }

    @Override
    public @NonNull Component asComponent() {
        if (component != null) return component;
        return component = BMM.deserialize(line).decoration(TextDecoration.ITALIC, false);
    }
}
