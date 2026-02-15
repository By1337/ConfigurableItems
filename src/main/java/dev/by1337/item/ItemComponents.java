package dev.by1337.item;

import dev.by1337.core.ServerVersion;
import dev.by1337.core.util.text.component.SourcedComponentLike;
import dev.by1337.item.component.BaseComponent;
import dev.by1337.item.component.ComponentsHolder;
import dev.by1337.item.component.impl.*;
import dev.by1337.item.util.ColorHolder;
import dev.by1337.item.util.Holder;
import dev.by1337.item.util.IntHolder;
import dev.by1337.item.util.dfu.YamlUpdater;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemComponents {
    private static final List<BaseComponent<?>> COMPONENTS = new ArrayList<>();

    public static final BaseComponent<ItemLoreComponent> LORE = register("lore", ItemLoreComponent.CODEC);
    public static final BaseComponent<ComponentLike> NAME = register("name", SourcedComponentLike.COMPONENT_LIKE_CODEC);
    public static final BaseComponent<IntHolder> AMOUNT = register("amount", IntHolder.CODEC);
    public static final BaseComponent<IntHolder> DAMAGE = register("damage", IntHolder.CODEC);
    public static final BaseComponent<MaterialComponent> MATERIAL = register("material", MaterialComponent.CODEC);
    public static final BaseComponent<CustomModelDataComponent> MODEL_DATA = register("model_data", CustomModelDataComponent.CODEC);
    public static final BaseComponent<PotionContentsComponent> POTION_CONTENTS = register("potion_contents", PotionContentsComponent.CODEC);
    public static final BaseComponent<ColorHolder> COLOR = register("color", ColorHolder.CODEC);
    public static final BaseComponent<EnchantmentsComponent> ENCHANTMENTS = register("enchantments", EnchantmentsComponent.CODEC);
    public static final BaseComponent<Boolean> UNBREAKABLE = register("unbreakable", YamlCodec.BOOL);
    public static final BaseComponent<ContainerComponent> CONTAINER = register("container", ContainerComponent.CODEC);
    public static final BaseComponent<HideFlagsComponents> HIDE_FLAGS = register("item_flags", HideFlagsComponents.CODEC);
    public static final BaseComponent<BasePotionComponent> BASE_POTION = register("potion", BasePotionComponent.CODEC);
    public static final BaseComponent<AttributesComponent> ATTRIBUTES = register("attributes", AttributesComponent.CODEC);
    //1.19.4+
    @Nullable
    public static final BaseComponent<ArmorTrimComponent> TRIM = register("trim", ArmorTrimComponent.CODEC, ArmorTrimComponent.CODEC != null);
    //1.20.5+
    @Nullable
    public static final BaseComponent<Boolean> HIDE_TOOLTIP = register("hide_tooltip", YamlCodec.BOOL, ServerVersion.is1_20_6orNewer());
    @Nullable
    public static final BaseComponent<Integer> MAX_STACK_SIZE = register("max_stack_size", YamlCodec.INT, ServerVersion.is1_20_6orNewer());
    @Nullable
    public static final BaseComponent<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register("enchantment_glint_override", YamlCodec.BOOL, ServerVersion.is1_20_6orNewer());
    //1.21.3+
    @Nullable
    public static final BaseComponent<Boolean> GLIDER = register("glider", YamlCodec.BOOL, ServerVersion.is1_21_3orNewer());
    //1.21.3+
    @Nullable
    public static final BaseComponent<NamespacedKey> TOOLTIP_STYLE = register("tooltip_style", BukkitCodecs.namespaced_key(), ServerVersion.is1_21_3orNewer());

    public static final YamlCodec<ComponentsHolder> COMPONENTS_CODEC;

    @Nullable
    private static <T> BaseComponent<T> register(String name, YamlCodec<T> codec, boolean supplier) {
        if (supplier) {
            return register(name, codec);
        }
        return null;
    }

    private static <T> BaseComponent<T> register(String name, YamlCodec<T> codec) {
        int id = COMPONENTS.size();
        var component = new BaseComponent<>(id, name, codec);
        COMPONENTS.add(component);
        return component;
    }

    public static int count() {
        return COMPONENTS.size();
    }

    public static List<BaseComponent<?>> list() {
        return Collections.unmodifiableList(COMPONENTS);
    }

    static {
        var builder = PipelineYamlCodecBuilder.of(ComponentsHolder::new);
        //noinspection rawtypes
        for (BaseComponent component : COMPONENTS) {
            //noinspection unchecked
            builder.field(component.codec(), component.name(),
                    v -> v.get(component),
                    (v, c) -> v.set(component, c)
            );
        }
        COMPONENTS_CODEC = builder
                .build()
                .preDecode(v -> {
                    var res = v.asYamlMap();
                    var map = res.result();
                    if (map != null) {
                        YamlUpdater.fixItem(map);
                        return YamlValue.wrap(map);
                    }
                    return v;
                })
        ;
    }
    @ApiStatus.Internal // может быть перенести?
    static ComponentsHolder fromItemStack(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return new ComponentsHolder();
        ComponentsHolder result = new ComponentsHolder();
        var lore = im.lore();
        if (lore != null) {
            result.set(ItemComponents.LORE, new ItemLoreComponent(new ArrayList<>(lore)));
        }
        var name = im.displayName();
        if (name != null) {
            result.set(ItemComponents.NAME, name);
        }
        result.set(ItemComponents.AMOUNT, new IntHolder(Integer.toString(itemStack.getAmount())));
        if (im instanceof Damageable damageable) {
            result.set(ItemComponents.DAMAGE, new IntHolder(Integer.toString(damageable.getDamage())));
        }
        var attributes = im.getAttributeModifiers();
        if (attributes != null) {
            List<AttributesComponent.Entry> modifiers = new ArrayList<>();
            attributes.forEach((k, v) -> modifiers.add(new AttributesComponent.Entry(k, v)));
            result.set(ItemComponents.ATTRIBUTES, new AttributesComponent(modifiers));
        }
        //todo skulls
        String material = itemStack.getType().getKey().asString();
        result.set(ItemComponents.MATERIAL, new MaterialComponent(material));
        if (ServerVersion.is1_21_4orNewer()) {
            //noinspection all
            var v = im.getCustomModelDataComponent();
            //noinspection all
            if (v != null) {
                //noinspection all
                result.set(ItemComponents.MODEL_DATA, new CustomModelDataComponent(
                        v.getFloats(),
                        v.getFlags(),
                        v.getStrings(),
                        v.getColors()
                ));
            }
        } else if (im.hasCustomModelData()) {
            var v = im.getCustomModelData();
            result.set(ItemComponents.MODEL_DATA, new CustomModelDataComponent(
                    List.of(((Integer) v).floatValue()),
                    List.of(),
                    List.of(),
                    List.of()
            ));
        }
        if (im instanceof PotionMeta potionMeta) {
            if (potionMeta.hasCustomEffects()) {
                result.set(ItemComponents.POTION_CONTENTS, new PotionContentsComponent(potionMeta.getCustomEffects()));
            }
            if (potionMeta.hasColor()) {
                result.set(ItemComponents.COLOR, ColorHolder.fromBukkit(potionMeta.getColor()));
            }
            result.set(ItemComponents.BASE_POTION, BasePotionComponent.fromMeta(potionMeta));
        }
        if (im instanceof LeatherArmorMeta m) {
            result.set(ItemComponents.COLOR, ColorHolder.fromBukkit(m.getColor()));
        }
        {
            var map = im.getEnchants();
            if (!map.isEmpty()) {
                result.set(ItemComponents.ENCHANTMENTS, EnchantmentsComponent.fromMap(map));
            }
        }
        if (im.isUnbreakable()) {
            result.set(ItemComponents.UNBREAKABLE, true);
        }
        if (im instanceof BlockStateMeta state && state instanceof Container container) {
            Int2ObjectOpenHashMap<ItemModel> map = new Int2ObjectOpenHashMap<>();
            var inv = container.getInventory();
            var arr = inv.getStorageContents();
            for (int i = 0; i < arr.length; i++) {
                var item = arr[i];
                if (item == null || item.getType().isAir()) continue;
                map.put(i, ItemModel.fromItemStack(item));
            }
            result.set(ItemComponents.CONTAINER, new ContainerComponent(map));
        }
        if (ItemComponents.TRIM != null) {
            //noinspection all
            if (im instanceof ArmorMeta armorMeta) {
                //noinspection all
                var v = armorMeta.getTrim();
                if (v != null) {
                    //noinspection all
                    result.set(ItemComponents.TRIM, new ArmorTrimComponent(new Holder<>(v.getMaterial()), new Holder<>(v.getPattern())));
                }
            }
        }
        if (ServerVersion.is1_20_6orNewer()) {
            if (im.isHideTooltip()) {
                result.set(ItemComponents.HIDE_TOOLTIP, true);
            }
            if (im.getEnchantmentGlintOverride()) {
                result.set(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
            if (im.hasMaxStackSize()) {
                result.set(ItemComponents.MAX_STACK_SIZE, im.getMaxStackSize());
            }
            if (ServerVersion.is1_21_3orNewer()) {
                if (im.isGlider()) {
                    result.set(ItemComponents.GLIDER, true);
                }
                if (im.hasTooltipStyle()) {
                    result.set(ItemComponents.TOOLTIP_STYLE, im.getTooltipStyle());
                }

            }
        }
        var set = im.getItemFlags();
        if (!set.isEmpty()) {
            result.set(ItemComponents.HIDE_FLAGS, new HideFlagsComponents(set));
        }
        return result;
    }
}
