package dev.by1337.item.component;

import dev.by1337.item.ItemComponents;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ComponentsHolder {
    private static final int INIT_SIZE = ItemComponents.count();
    private final Object[] components;

    public ComponentsHolder() {
        components = new Object[INIT_SIZE];
    }

    private ComponentsHolder(Object[] components) {
        this.components = components;
    }

    public boolean getBool(@Nullable BaseComponent<Boolean> type) {
        return get(type, false);
    }

    @Nullable
    public <T> T get(@Nullable BaseComponent<T> type) {
        if (type == null) return null;
        //noinspection unchecked
        return (T) components[type.id()];
    }

    @Nullable
    @Contract(pure = true, value = "_, !null -> !null")
    public <T> T get(@Nullable BaseComponent<T> type, T def) {
        if (type == null) return def;
        var o = components[type.id()];
        //noinspection unchecked
        return o == null ? def : (T) o;
    }

    public <T> void set(BaseComponent<T> type, T value) {
        if (type == null) return;
        components[type.id()] = value;
    }

    public void setDefaults(ComponentsHolder def) {
        for (int i = 0; i < components.length; i++) {
            if (components[i] == null) {
                components[i] = def.components[i];
            }
        }
    }

    @Contract(pure = true, value = " -> new")
    public ComponentsHolder copy() {
        return new ComponentsHolder(Arrays.copyOf(components, INIT_SIZE));
    }

    @Contract(pure = true, value = "_ -> new")
    public ComponentsHolder merge(ComponentsHolder other) {
        Object[] result = Arrays.copyOf(components, INIT_SIZE);
        for (int i = 0; i < other.components.length; i++) {
            Object o = other.components[i];
            if (o != null) {
                Object current = components[i];
                if (current == null) {
                    result[i] = o;
                } else if (o instanceof MergeableComponent<?>) {
                    //noinspection rawtypes, unchecked
                    result[i] = merge((MergeableComponent) current, (MergeableComponent) o);
                }
            }
        }
        return new ComponentsHolder(result);
    }

    private <T extends MergeableComponent<T>> T merge(T t, T t2) {
        return t.and(t2);
    }
}
