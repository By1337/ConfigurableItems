package dev.by1337.item.util.dfu;

import dev.by1337.yaml.YamlMap;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class YamlUpdaterTest {

    @Test
    void enchantmentsUpdater(){
        String yaml = """
                item:
                  enchantments:
                    - protection;1
                    - slow;3
                """;
        YamlMap map = YamlMap.load(new StringReader(yaml));
        YamlUpdater.fixItem(map.get("item").asYamlMap().getOrThrow());
        assertEquals("1", map.get("item.enchantments.protection").asString(""));
        assertEquals("3", map.get("item.enchantments.slow").asString(""));
    }

    @Test
    void potionContentsUpdater(){
        String yaml = """
                item:
                  potion_effects:
                    - "speed;20;0"
                    - "slow;13;37"
                """;
        YamlMap map = YamlMap.load(new StringReader(yaml));
        YamlUpdater.fixItem(map.get("item").asYamlMap().getOrThrow());
        System.out.println(map.saveToString());
        assertEquals("20 0", map.get("item.potion_contents.speed").asString(""));
        assertEquals("13 37", map.get("item.potion_contents.slowness").asString(""));
    }
    @Test
    void noUpdater(){
        String yaml = """
                item:
                  enchantments:
                    protection: '1'
                    slow: '3'
                  potion_contents:
                    speed: 20 0
                    slow: 13 37
                """;
        YamlMap map = YamlMap.load(new StringReader(yaml));
        YamlUpdater.fixItem(map.get("item").asYamlMap().getOrThrow());
        assertEquals("20 0", map.get("item.potion_contents.speed").asString(""));
        assertEquals("13 37", map.get("item.potion_contents.slowness").asString(""));
        assertEquals("1", map.get("item.enchantments.protection").asString(""));
        assertEquals("3", map.get("item.enchantments.slow").asString(""));
    }
}