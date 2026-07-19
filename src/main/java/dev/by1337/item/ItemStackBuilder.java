package dev.by1337.item;

import dev.by1337.plc.PlaceholderApplier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ItemStackBuilder {
    public static ItemStack build(ItemModel model, PlaceholderApplier placeholders) {
        return build(model, placeholders, null);
    }

    public static ItemStack build(ItemModel model, PlaceholderApplier placeholders, @Nullable Locale locale) {
        return ItemStackRenderer.render(model, placeholders, locale, null);
    }
}
