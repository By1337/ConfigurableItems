package dev.by1337.item;

import com.google.common.cache.Cache;
import dev.by1337.item.component.impl.MaterialComponent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MaterialComponentTest {
    @Test
    void stripsWhitespaceAfterResolvingMaterialPlaceholders() {
        var material = new MaterialComponent(" %bthemes_gui_material_1% ");
        assertEquals("blue_stained_glass_pane", material.resolve(s -> "  blue_stained_glass_pane  "));
    }

    @Test
    void dynamicRegistryLookupReevaluatesPlaceholdersInsteadOfCachingFirstMiss() {
        var material = new MaterialComponent("%bthemes_gui_material_1%");
        AtomicInteger calls = new AtomicInteger();
        assertNull(material.fromGlobalRegistry(s -> { calls.incrementAndGet(); return "stone"; }));
        assertNull(material.fromGlobalRegistry(s -> { calls.incrementAndGet(); return "dirt"; }));
        assertEquals(2, calls.get());
        assertFalse(material.isFinal());
    }

    @Test
    @SuppressWarnings("unchecked")
    void resolvedMaterialCacheReturnsIndependentClonesAndKeepsDifferentMaterialsSeparate() throws Exception {
        var field = MaterialComponent.class.getDeclaredField("TEMPLATES");
        field.setAccessible(true);
        Cache<String, ItemStack> templates = (Cache<String, ItemStack>) field.get(null);
        String first = "test-template-first";
        String second = "test-template-second";
        templates.put(first, new TestStack(1));
        templates.put(second, new TestStack(2));
        try {
            var material = new MaterialComponent("{material}");
            ItemStack rendered = material.create(s -> first);
            rendered.setAmount(50);
            assertEquals(1, material.create(s -> first).getAmount());
            assertEquals(2, material.create(s -> second).getAmount());
            assertNotSame(templates.getIfPresent(first), material.create(s -> first));
        } finally {
            templates.invalidate(first);
            templates.invalidate(second);
        }
    }

    private static class TestStack extends ItemStack {
        private int count;
        TestStack(int count) { this.count = count; }
        @Override public int getAmount() { return count; }
        @Override public void setAmount(int amount) { count = amount; }
        @Override public TestStack clone() { return new TestStack(count); }
    }
}
