package dev.by1337.item.registry;

import dev.by1337.item.ItemModel;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GlobalItemRegistry {
    private static final Map<String, ItemRegistry<?>> REGISTRIES = new HashMap<>();

    public static <T extends ItemModelHolder> void register(@NotNull ItemRegistry<T> registry) {
        REGISTRIES.put(registry.space(), registry);
    }

    @Nullable
    public static <T extends ItemModelHolder> ItemRegistry<T> get(String space) {
        return (ItemRegistry<T>) REGISTRIES.get(space);
    }

    public static @Nullable String getIdByItem(@Nullable ItemStack item) {
        if (item == null) return null;
        if (!item.hasItemMeta()) return null;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        var registry = pdc.get(ItemRegistry.REGISTRY_KEY, PersistentDataType.STRING);
        if (registry == null) return null;
        var key = NamespacedKey.fromString(registry + ":type");
        if (key == null) return null;
        var id = pdc.get(key, PersistentDataType.STRING);
        if (id == null) return null;
        return registry + ":" + id;
    }

    public static @Nullable ItemModel resolveItemModel(String fullId) {
        var split = fullId.split(":", 2);
        if (split.length != 2) return null;

        var registry = REGISTRIES.get(split[0]);
        if (registry == null) return null;

        var v = registry.byId(split[1]);
        if (v == null) return null;
        return v.itemModel();
    }

    public static <T extends ItemModelHolder> @Nullable ItemStack resolveItemStack(String fullId) {
        var split = fullId.split(":", 2);
        if (split.length != 2) return null;

        ItemRegistry<T> registry = (ItemRegistry<T>) REGISTRIES.get(split[0]);
        if (registry == null) return null;

        T v = registry.byId(split[1]);
        if (v == null) return null;
        return registry.getItemStack(v);
    }

    public static int size() {
        return REGISTRIES.size();
    }

    public static Set<String> keySet() {
        return Collections.unmodifiableSet(REGISTRIES.keySet());
    }

    public static Collection<ItemRegistry<?>> values() {
        return Collections.unmodifiableCollection(REGISTRIES.values());
    }
}
