package dev.by1337.item.util;

import org.jetbrains.annotations.Nullable;
import dev.by1337.plc.PlaceholderApplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class DecodableHolder<T> {
    private static final Logger log = LoggerFactory.getLogger(DecodableHolder.class);
    private String src;
    private final Function<String, T> decoder;
    private boolean cashed;
    private T value;

    public DecodableHolder(T value) {
        this.value = value;
        decoder = s -> this.value;
        cashed = true;
    }


    public DecodableHolder(String src, Function<String, T> decoder) {
        this.src = src;
        this.decoder = decoder;
        if (hasNoPlaceholders(src)) {
            value = tryGet();
            cashed = value != null;
        }
    }

    public @Nullable T tryGet() {
        return tryGet(s -> s);
    }

    public @Nullable T tryGet(PlaceholderApplier placeholders) {
        if (cashed) return value;
        String resolved = src;
        try {
            resolved = placeholders.setPlaceholders(src);
            return decoder.apply(resolved);
        } catch (Exception e) {
            log.error("Failed to parse {}", resolved, e);
        }
        return null;
    }

    public boolean isFinal() {
        return cashed;
    }

    private static boolean hasNoPlaceholders(String input) {
        return !input.contains("{") && !input.contains("%");
    }
}
