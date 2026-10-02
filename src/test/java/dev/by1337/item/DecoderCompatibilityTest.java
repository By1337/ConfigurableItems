package dev.by1337.item;

import dev.by1337.item.component.impl.ItemLoreComponent;
import dev.by1337.item.util.IntHolder;
import dev.by1337.yaml.YamlValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DecoderCompatibilityTest {
    @Test
    void publicCodecsDelegateToDecoders() {
        var amount = YamlValue.wrap("42");
        assertEquals(IntHolder.DECODER.decode(amount).result().src(), IntHolder.CODEC.decode(amount).result().src());

        var lore = YamlValue.wrap(List.of("first", "second"));
        ItemLoreComponent decoded = ItemLoreComponent.DECODER.decode(lore).result();
        ItemLoreComponent legacy = ItemLoreComponent.CODEC.decode(lore).result();
        assertNotNull(decoded);
        assertNotNull(legacy);
        assertEquals(decoded.lore().size(), legacy.lore().size());
    }
}
