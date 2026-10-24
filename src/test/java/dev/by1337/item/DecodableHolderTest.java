package dev.by1337.item;

import dev.by1337.item.util.DecodableHolder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DecodableHolderTest {
    @Test
    void resolvesPapiBeforeDecodingAndDoesNotFreezeDifferentPlayersColors() {
        AtomicReference<String> hex = new AtomicReference<>("#FFC300");
        DecodableHolder<Integer> color = new DecodableHolder<>("%bthemes_hex_color%", value -> Integer.parseInt(value.substring(1), 16));
        assertFalse(color.isFinal());
        assertEquals(0xFFC300, color.tryGet(source -> source.replace("%bthemes_hex_color%", hex.get())));
        hex.set("#7A99FF");
        assertEquals(0x7A99FF, color.tryGet(source -> source.replace("%bthemes_hex_color%", hex.get())));
        assertFalse(color.isFinal());
    }

    @Test
    void staticValuesRemainCachedAndBypassCallerPlaceholders() {
        AtomicInteger decoded = new AtomicInteger();
        DecodableHolder<Integer> color = new DecodableHolder<>("#112233", value -> {
            decoded.incrementAndGet();
            return Integer.parseInt(value.substring(1), 16);
        });
        assertTrue(color.isFinal());
        assertEquals(0x112233, color.tryGet(source -> { fail("Static values are already cached"); return source; }));
        assertEquals(0x112233, color.tryGet());
        assertEquals(1, decoded.get());
    }
}
