package dev.by1337.item;

import dev.by1337.item.component.BaseComponent;
import dev.by1337.item.component.ComponentsHolder;
import dev.by1337.item.component.impl.MaterialComponent;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class ItemModel {
    public static final YamlCodec<ItemModel> CODEC;
    public static final ItemModel AIR;

    private final ComponentsHolder components;
    private boolean dirty = false;
    private ItemStack cached;

    public ItemModel(ComponentsHolder components) {
        this.components = components;
    }

    public static ItemModel fromItemStack(ItemStack itemStack) {
        return new ItemModel(ItemComponents.fromItemStack(itemStack));
    }

    public static ItemModel ofMaterial(String material) {
        if (material.equalsIgnoreCase("air")) return AIR;
        var result = new ItemModel(new ComponentsHolder());
        result.components.set(ItemComponents.MATERIAL, new MaterialComponent(material));
        return result;
    }

    @Contract(pure = true, value = "_ -> new")
    public ItemModel and(ItemModel i) {
        return new ItemModel(components.merge(i.components));
    }

    @Contract(pure = true, value = " -> new")
    public ItemModel copy() {
        return new ItemModel(components.copy());
    }

    @Contract(pure = true)
    public ComponentsHolder components() {
        return components.copy();
    }

    @Contract(pure = true, value = "_ -> new")
    public ItemModel setDefaults(ItemModel def) {
        var c = components.copy();
        c.setDefaults(def.components);
        return new ItemModel(c);
    }

    @Contract(pure = true)
    public boolean getBool(@Nullable BaseComponent<Boolean> type) {
        return components.getBool(type);
    }

    @Nullable
    @Contract(pure = true)
    public <T> T get(@Nullable BaseComponent<T> type) {
        return components.get(type);
    }

    @Nullable
    @Contract(pure = true, value = "_, !null -> !null")
    public <T> T get(@Nullable BaseComponent<T> type, T def) {
        return components.get(type, def);
    }


    public ItemStack build() {
        return build(s -> s);
    }

    public ItemStack build(PlaceholderApplier placeholders) {
        return ItemStackBuilder.build(this, placeholders);
    }

    @ApiStatus.Experimental
    @ApiStatus.Internal
    public boolean dirty() {
        return dirty;
    }

    @ApiStatus.Experimental
    @ApiStatus.Internal
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    ItemStack cached() {
        return cached;
    }

    void setCached(ItemStack cached) {
        this.cached = cached;
    }

    static {
        AIR = new ItemModel(new ComponentsHolder());
        AIR.components.set(ItemComponents.MATERIAL, new MaterialComponent("air"));
        CODEC = ItemComponents.COMPONENTS_CODEC.map(
                ItemModel::new,
                i -> i.components
        );
    }
}
