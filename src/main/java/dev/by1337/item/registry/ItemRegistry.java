package dev.by1337.item.registry;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class ItemRegistry<T extends ItemModelHolder> implements Iterable<T> {
    public static <T extends ItemModelHolder> YamlCodec<ItemRegistry<T>> codec(String space, YamlCodec<T> codec) {
        return YamlCodec.mapOf(YamlCodec.STRING, codec)
                .map(m -> new ItemRegistry<>(space, m), v -> v.idToItem);
    }

    public static final @NotNull NamespacedKey REGISTRY_KEY = Objects.requireNonNull(NamespacedKey.fromString("registry:type"));
    private final Map<String, T> idToItem = new HashMap<>();
    private final Map<String, ItemStack> idToItemStack = new HashMap<>();
    private final Map<T, String> itemToId = new IdentityHashMap<>();
    private final String space;
    private final NamespacedKey key;
    private Function<PersistentDataContainer, @Nullable String> nbtRemapper;

    public ItemRegistry(String space) {
        this.space = space;
        //noinspection deprecation
        key = new NamespacedKey(space, "type");
    }

    public ItemRegistry(String space, Map<String, T> m) {
        this.space = space;
        //noinspection deprecation
        key = new NamespacedKey(space, "type");
        m.forEach(this::register);
    }

    @CanIgnoreReturnValue
    public T register(final String name, T value) {
        if (idToItem.containsKey(name)) {
            throw new IllegalStateException(name + " is already registered.");
        }
        var maybe = itemToId.get(value);
        if (maybe != null) {
            throw new IllegalStateException(value + " already registered under that name " + maybe);
        }
        idToItem.put(name, value);
        itemToId.put(value, name);
        return value;
    }

    @Contract("null -> null")
    public @Nullable T getByItem(@Nullable ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return null;
        var pdc = im.getPersistentDataContainer();
        var id = pdc.get(key, PersistentDataType.STRING);
        if (id == null && nbtRemapper != null) return byId(nbtRemapper.apply(pdc));
        return byId(id);
    }

    public @NotNull ItemStack getItemStack(@NotNull T value) {
        String id = itemToId.get(value);
        Objects.requireNonNull(id, "Cannot get ItemStack for unregistered item: " + value);
        ItemStack itemStack = idToItemStack.get(id);
        if (itemStack != null) return itemStack.clone();
        itemStack = value.itemModel().build();
        itemStack.editMeta(m -> {
            var pdc = m.getPersistentDataContainer();
            pdc.set(key, PersistentDataType.STRING, id);
            pdc.set(REGISTRY_KEY, PersistentDataType.STRING, space);
        });
        idToItemStack.put(id, itemStack);
        return itemStack.clone();
    }

    @Contract("null -> null")
    public @Nullable T byId(@Nullable String name) {
        if (name == null) return null;
        return idToItem.get(name);
    }

    public @NotNull String getId(@NotNull T value) {
        return Objects.requireNonNull(itemToId.get(value), "Unable to get id of unregistered type " + value);
    }

    @Contract("_, !null -> !null")
    public @Nullable String getIdOr(@NotNull T value, @Nullable String def) {
        return itemToId.getOrDefault(value, def);
    }

    @Contract("null -> null")
    public String getIdOrNull(@Nullable T value) {
        if (value == null) return null;
        return itemToId.get(value);
    }

    public @Nullable String getIdOrNull(@Nullable ItemStack itemStack) {
        T t = getByItem(itemStack);
        if (t == null) return null;
        return getIdOrNull(t);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return idToItem.values().iterator();
    }

    public Stream<T> stream() {
        return idToItem.values().stream();
    }

    public Set<String> ids() {
        return Collections.unmodifiableSet(idToItem.keySet());
    }

    public void clear() {
        idToItem.clear();
        itemToId.clear();
        idToItemStack.clear();
    }

    public String space() {
        return space;
    }

    public void setNbtRemapper(Function<PersistentDataContainer, @Nullable String> nbtRemapper) {
        this.nbtRemapper = nbtRemapper;
    }

    public Map<String, T> idToItem() {
        return Collections.unmodifiableMap(idToItem);
    }

    public Map<T, String> itemToId() {
        return Collections.unmodifiableMap(itemToId);
    }
}

