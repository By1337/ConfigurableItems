package dev.by1337.item.component.impl;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.by1337.plc.PlaceholderApplier;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MaterialComponent {
    public static final YamlCodec<MaterialComponent> CODEC =YamlCodec.STRING.schema(s -> s.or(BukkitCodecs.material().schema()))
            .map(MaterialComponent::new, MaterialComponent::input);
    public static final MaterialComponent DEFAULT = new MaterialComponent("dirt");
    private static final Logger log = LoggerFactory.getLogger("CfgItems");
    private final String input;
    private @Nullable ItemStack cashed;
    private final boolean mutable;

    public MaterialComponent(String input) {
        this.input = input;
        mutable = hasNoPlaceholders(input);
    }

    public ItemStack create(PlaceholderApplier placeholders) {
        if (cashed != null) return cashed.clone();
        ItemStack result = Builder.build(placeholders.setPlaceholders(input));
        if (!mutable) {
            cashed = result.clone();
        }
        return result;
    }
    public boolean isFinal(){
        return !mutable;
    }

    private static boolean hasNoPlaceholders(String input) {
        return !input.contains("{") && !input.contains("%");
    }

    public String input() {
        return input;
    }

    private static class Builder {

        private static final YamlCodec<Material> MATERIAL = BukkitCodecs.material();

        private static ItemStack build(String input) {
            if (input.startsWith("basehead-")) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                String url = input.substring("basehead-".length());
                head.editMeta(m -> {
                    PlayerProfile profile = Bukkit.createProfile(
                            UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)));
                    profile.setProperty(new ProfileProperty("textures", url));
                    ((SkullMeta) (m)).setPlayerProfile(
                            profile
                    );
                });
                return head;
            } else if (input.startsWith("player-")) {
                String value = input.substring("player-".length());
                if (value.length() == 36) {
                    try {
                        UUID uuid = UUID.fromString(value);
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        head.editMeta(m -> ((SkullMeta) (m)).setOwningPlayer(Bukkit.getOfflinePlayer(uuid)));
                        return head;
                    } catch (Exception ignore) {
                    }
                }
                Player player = Bukkit.getPlayer(value);
                if (player == null) {
                    log.error("Unknown player {}", value);
                    return new ItemStack(Material.PLAYER_HEAD);
                }
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                head.editMeta(m -> ((SkullMeta) (m)).setOwningPlayer(player));
                return head;
            } else {
                DataResult<Material> res = MATERIAL.decode(YamlValue.wrap(input));
                if (res.hasResult()) return new ItemStack(res.result());
                log.error("Failed to create item {}", input);
                return new ItemStack(Material.DIRT);
            }
        }
    }
}
