package dev.by1337.item;

import dev.by1337.core.ServerVersion;
import dev.by1337.core.util.text.component.RawTextComponent;
import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.by1337.item.component.impl.AttributesComponent;
import dev.by1337.item.component.impl.ContainerComponent;
import dev.by1337.item.component.impl.MaterialComponent;
import dev.by1337.item.util.IntHolder;
import dev.by1337.plc.PlaceholderApplier;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemStackBuilder {
    public static ItemStack build(ItemModel model, PlaceholderApplier placeholders) {
        var cache = model.cached();
        if (cache != null && !model.dirty()) {
            return build(model, placeholders, cache);
        }
        var material = model.get(ItemComponents.MATERIAL, MaterialComponent.DEFAULT);
        ItemStack result = material.create(placeholders);
        ItemMeta im = result.getItemMeta();
        if (im == null) {
            return result;
        }
        var attributes = model.get(ItemComponents.ATTRIBUTES);
        if (attributes != null){
            for (AttributesComponent.Entry entry : attributes.modifiers()) {
                im.addAttributeModifier(entry.attribute(), entry.modifier());
            }
        }

        var hide = model.get(ItemComponents.HIDE_FLAGS);
        if (hide != null) {
            hide.flags().forEach(im::addItemFlags);
            if (attributes == null && ServerVersion.is1_20_5orNewer() && im.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)) {
                // https://github.com/PaperMC/Paper/issues/10655
                im.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("123", 1, AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        var potion = model.get(ItemComponents.POTION_CONTENTS);
        if (potion != null) {
            for (PotionEffect potionEffect : potion.contents()) {
                if (im instanceof PotionMeta potionMeta) {
                    potionMeta.addCustomEffect(potionEffect, true);
                } else if (im instanceof Arrow arrow) {
                    arrow.addCustomEffect(potionEffect, true);
                } else if (im instanceof SuspiciousStewMeta m) {
                    m.addCustomEffect(potionEffect, true);
                }
            }
        }
        var basePotion = model.get(ItemComponents.BASE_POTION);
        if (basePotion != null && im instanceof PotionMeta pm) {
            basePotion.apply(pm);
        }

        var color0 = model.get(ItemComponents.COLOR);
        if (color0 != null) {
            var color = color0.toBukkit();
            if (im instanceof TropicalFishBucketMeta buket) {
                var v = DyeColor.getByColor(color);
                if (v != null) buket.setBodyColor(v);
            } else if (im instanceof PotionMeta pm) {
                pm.setColor(color);
            } else if (im instanceof MapMeta map) {
                map.setColor(color);
            } else if (im instanceof LeatherArmorMeta m) {
                m.setColor(color);
            } else if (im instanceof FireworkEffectMeta effectMeta) {
                effectMeta.setEffect(FireworkEffect.builder().withColor(color).build());
            }
        }
        var enchantments = model.get(ItemComponents.ENCHANTMENTS);
        if (enchantments != null) {
            for (var entry : enchantments.enchantments()) {
                im.addEnchant(entry.enchantment(), entry.lvl(), true);
            }
        }

        var modelData = model.get(ItemComponents.MODEL_DATA);
        if (modelData != null) {
            if (ServerVersion.is1_21_4orNewer()) {
                //noinspection all
                var kringe = im.getCustomModelDataComponent();
                //noinspection all
                kringe.setFloats(modelData.floats());
                //noinspection all
                kringe.setFlags(modelData.flags());
                //noinspection all
                kringe.setStrings(modelData.strings());
                //noinspection all
                kringe.setColors(modelData.colors());
                //noinspection all
                im.setCustomModelDataComponent(kringe);
            } else {
                im.setCustomModelData(modelData.floats().get(0).intValue());
            }
        }
        if (ServerVersion.is1_20_6orNewer()) {
            if (model.getBool(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE)) {
                im.setEnchantmentGlintOverride(true);
            }
            if (model.getBool(ItemComponents.HIDE_TOOLTIP)) {
                im.setHideTooltip(true);
            }
            Integer maxStackSize = model.get(ItemComponents.MAX_STACK_SIZE);
            if (maxStackSize != null) {
                im.setMaxStackSize(maxStackSize);
            }
        }
        if (ServerVersion.is1_19_4orNewer()) {
            //JIT DCE?
            var armorTrim = model.get(ItemComponents.TRIM);
            if (armorTrim != null && armorTrim.has()) {
                //noinspection all
                if (im instanceof ArmorMeta armorMeta) {
                    //noinspection all
                    armorMeta.setTrim(armorTrim.armorTrim().get());
                }
            }
        }

        if (ServerVersion.is1_21_3orNewer()) {
            if (model.getBool(ItemComponents.GLIDER)){
                im.setGlider(true);
            }
            var tooltip_style = model.get(ItemComponents.TOOLTIP_STYLE);
            if (tooltip_style != null){
                im.setTooltipStyle(tooltip_style);
            }
        }
        if (model.getBool(ItemComponents.UNBREAKABLE)) {
            im.setUnbreakable(true);
        }

        var name = model.get(ItemComponents.NAME);
        if (name != null) {
            im.displayName(toComponent(name, placeholders));
        }
        var lore = model.get(ItemComponents.LORE);
        if (lore != null) {
            List<Component> loreComponents = new ArrayList<>();
            lore.forEachLore(line -> applyComponent(line, placeholders, loreComponents::add));
            im.lore(loreComponents);
        }
        if (im instanceof Damageable damageable) {
            damageable.setDamage(model.get(ItemComponents.DAMAGE, IntHolder.ZERO).getOrDefault(placeholders, 0));
        }
        if (im instanceof BlockStateMeta bsm) {
            BlockState state = bsm.getBlockState();
            if (state instanceof Container container) {
                ContainerComponent c = model.get(ItemComponents.CONTAINER);
                if (c != null) {
                    for (Int2ObjectMap.Entry<ItemModel> entry : c.items().int2ObjectEntrySet()) {
                        container.getInventory().setItem(entry.getIntKey(), build(entry.getValue(), placeholders));
                    }
                }
                bsm.setBlockState(container);
            }
        }

        result.setItemMeta(im);
        result.setAmount(model.get(ItemComponents.AMOUNT, IntHolder.ONE).getOrDefault(placeholders, 1));
        model.setCached(result.clone());
        model.setDirty(false);
        return result;
    }

    private static ItemStack build(ItemModel model, PlaceholderApplier placeholders, ItemStack cache) {
        var result = cache.clone();
        var meta = result.getItemMeta();
        if (meta != null) {
            if (meta instanceof Damageable damageable) {
                damageable.setDamage(model.get(ItemComponents.DAMAGE, IntHolder.ZERO).getOrDefault(placeholders, 0));
            }
            var lore = model.get(ItemComponents.LORE);
            if (lore != null && lore.hasPlaceholders()) {
                List<Component> loreComponents = new ArrayList<>();
                lore.forEachLore(line -> applyComponent(line, placeholders, loreComponents::add));
                meta.lore(loreComponents);
            }
            var name = model.get(ItemComponents.NAME);
            if (name != null) {
                meta.displayName(toComponent(name, placeholders));
            }
            result.setItemMeta(meta);
        }
        result.setAmount(model.get(ItemComponents.AMOUNT, IntHolder.ONE).getOrDefault(placeholders, 1));
        return result;
    }

    private static boolean hasPlaceholders(String input) {
        return input.contains("{") || input.contains("%");
    }

    private static void applyComponent(ComponentLike c, PlaceholderApplier placeholders, Consumer<Component> processor) {
        if (c instanceof RawTextComponent raw) {
            String s = placeholders.setPlaceholders(raw.source());
            for (String line : s.split("\n")) {
                processor.accept(MiniMessage.deserialize(line).decoration(TextDecoration.ITALIC, false));
            }
        } else {
            processor.accept(c.asComponent().decoration(TextDecoration.ITALIC, false));
        }
    }

    private static Component toComponent(ComponentLike c, PlaceholderApplier placeholders) {
        if (c instanceof RawTextComponent c1) {
            return c1.asComponent(placeholders).decoration(TextDecoration.ITALIC, false);
        }
        return c.asComponent().decoration(TextDecoration.ITALIC, false);
    }
}
