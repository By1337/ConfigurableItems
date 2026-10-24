package dev.by1337.item;

import dev.by1337.item.component.BaseComponent;
import dev.by1337.item.component.ComponentsHolder;
import dev.by1337.item.component.impl.ItemLoreComponent;
import dev.by1337.item.component.impl.MaterialComponent;
import dev.by1337.item.util.IntHolder;
import dev.by1337.item.util.text.RawTextComponentLike;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.YamlDecoder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.Component;
import dev.by1337.edsl.text.LocalizedText;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class ItemModel {
    @Deprecated
    public static final YamlCodec<ItemModel> CODEC;
    public static final YamlDecoder<ItemModel> DECODER;
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
    public ItemModel withAmount(int amount) {
        return with(c -> c.set(ItemComponents.AMOUNT, new IntHolder(amount)));
    }

    @Contract(pure = true, value = "_ -> new")
    public ItemModel withAmount(String amount) {
        return with(c -> c.set(ItemComponents.AMOUNT, new IntHolder(amount)));
    }

    @Contract(pure = true, value = "_ -> new")
    public ItemModel withMaterial(String material) {
        return with(c -> c.set(ItemComponents.MATERIAL, new MaterialComponent(material)));
    }

    @Contract(pure = true, value = "_ -> new")
    public ItemModel with(Consumer<ComponentsHolder> consumer) {
        var c = components.copy();
        consumer.accept(c);
        return new ItemModel(c);
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

    @Contract(pure = true, value = "null -> false")
    public <T> boolean has(@Nullable BaseComponent<T> type) {
        return get(type) != null;
    }


    public ItemStack build() {
        return build(s -> s);
    }

    public ItemStack build(PlaceholderApplier placeholders) {
        return ItemStackBuilder.build(this, placeholders);
    }

    public ItemStack build(PlaceholderApplier placeholders, @Nullable Locale locale) {
        return ItemStackBuilder.build(this, placeholders, locale);
    }

    /** Rewrites freshly decoded registry text to persistent translation references. */
    @ApiStatus.Internal
    public void registerTranslations(String prefix, Map<String, ComponentLike> translations) {
        var name = get(ItemComponents.NAME);
        if (name != null) {
            String key = prefix + ".name";
            translations.put(key, translationText(name));
            components.set(ItemComponents.NAME, new RawTextComponentLike("<lang:" + key + ">"));
        }
        var lore = get(ItemComponents.LORE);
        if (lore != null) {
            var references = new ArrayList<ComponentLike>();
            int lines = lore.lore().size();
            for (var variant : lore.variants().values()) lines = Math.max(lines, variant.size());
            for (int i = 0; i < lines; i++) {
                String key = prefix + ".lore." + i;
                ComponentLike fallback = i < lore.lore().size() ? translationText(lore.lore().get(i)) : Component.empty();
                Map<Locale, ComponentLike> variants = new LinkedHashMap<>();
                for (var variant : lore.variants().entrySet()) {
                    var values = variant.getValue();
                    variants.put(variant.getKey(), i < values.size() ? translationText(values.get(i)) : Component.empty());
                }
                translations.put(key, variants.isEmpty() ? fallback : new LocalizedText(fallback, variants));
                references.add(new RawTextComponentLike("<lang:" + key + ">"));
            }
            components.set(ItemComponents.LORE, new ItemLoreComponent(references));
        }
        cached = null;
    }

    private static ComponentLike translationText(ComponentLike text) {
        if (text instanceof LocalizedText localized) return localized.map(ItemModel::translationText);
        if (text instanceof RawTextComponentLike raw) {
            return new dev.by1337.edsl.text.RawTextComponentLike(raw.line());
        }
        return text;
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
        DECODER = ItemComponents.COMPONENTS_DECODER.map(ItemModel::new);
        CODEC = YamlCodec.of(DECODER);
    }
}
