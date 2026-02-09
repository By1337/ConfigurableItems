package dev.by1337.item.util.dfu;

import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class YamlUpdater {
    private static final List<String> ALL_ITEM_FLAGS = Arrays.stream(ItemFlag.values()).map(Enum::name).toList();
    private static final String FIXER = "$" + YamlUpdater.class.getName().replace(".", "_");
    private static final Logger log = LoggerFactory.getLogger(YamlUpdater.class);

    public static void fixItem(YamlMap map) {
        if (map.has(FIXER)) return;
        map.set(FIXER, true);
        rename(map, "display_name", "name");
        rename(map, "potion_effects", "potion_contents");

        if (Objects.equals(map.getRaw("all_flags"), true)) {
            rename(map, "item_flags", "$item_flags");
            map.set("$all_flags", "apply");
            map.set("item_flags", ALL_ITEM_FLAGS);
        }
        update(map, "enchantments", YamlUpdater::enchantmentsUpdater);
        update(map, "potion_contents", v -> PotionContentsUpdater.update(map, v));
    }

    private static void update(YamlMap map, String who, Function<YamlValue, Object> updater) {
        if (!map.has(who)) return;
        var legacy = map.get(who);
        Object result = updater.apply(legacy);
        if (result == null) return;
        rename(map, who, "$" +Integer.toHexString(System.identityHashCode(legacy)));
        map.getRaw().put(who, result);
    }
    private static void rename(YamlMap map, String from, String to) {
        if (map.has(from)) {
            map.getRaw().put(to, map.getRaw().remove(from));
            map.set("$rename-" + from, to);
        }
    }

    // <enchantment>;<lvl> -> map{enchantment: lvl}
    private static Object enchantmentsUpdater(YamlValue value){
        if (value.isMap()) return null;
        DataResult<List<String>> legacy = YamlCodec.STRINGS.decode(value);
        if (!legacy.hasResult()){
            log.error("Failed to update enchantments cuz {}", legacy.error());
            return null;
        }
        List<String> list = legacy.getOrThrow();
        YamlMap result = new YamlMap();
        for (String s : list) {
            String[] args = s.split(";", 2);
            if (args.length != 2){
                log.error("Failed to update enchantment! Expected ‘<enchantment>;<lvl>’, but got ‘{}’.", s);
                continue;
            }
            result.set(args[0], args[1]);
        }
        return result.getRaw();
    }


    public static Object deepCopy(Object object) {
        if (object instanceof Map) {
            Map<String, Object> originalMap = (Map<String, Object>) object;
            Map<String, Object> copiedMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
                copiedMap.put(entry.getKey(), deepCopy(entry.getValue()));
            }
            return copiedMap;
        } else if (object instanceof Collection<?> originalList) {
            List<Object> copiedList = new ArrayList<>();
            for (Object item : originalList) {
                copiedList.add(deepCopy(item));
            }
            return copiedList;
        } else {
            return object;
        }
    }
}
