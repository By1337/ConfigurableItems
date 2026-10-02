package dev.by1337.item.component.impl;

import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import dev.by1337.yaml.decoder.k2v.LookupDecoder;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;
import java.util.function.Supplier;

public record AttributesComponent(List<Entry> modifiers) implements MergeableComponent<AttributesComponent> {
    public static YamlDecoder<AttributesComponent> DECODER = Entry.DECODER.listOf().map(AttributesComponent::new);
    @Deprecated
    public static YamlCodec<AttributesComponent> CODEC = YamlCodec.of(DECODER);

    public record Entry(Attribute attribute, AttributeModifier modifier) {
        public static final YamlDecoder<Entry> DECODER = RecordYamlDecoder.mapOf(
                Entry::new,
                Decoders.ATTRIBUTE_DECODER.fieldOf("attribute"),
                Decoders.ATTRIBUTE_MODIFIER_DECODER.fieldOf()
        );
        public static final YamlCodec<Entry> CODEC = YamlCodec.of(DECODER);
    }

    @Override
    public AttributesComponent and(AttributesComponent t1) {
        List<Entry> modifiers = new ArrayList<>(this.modifiers);
        modifiers.addAll(t1.modifiers);
        return new AttributesComponent(modifiers);
    }

    private static class Decoders {
        private static final YamlDecoder<Attribute> ATTRIBUTE_DECODER = make(() -> {
            Map<String, Attribute> normal = new HashMap<>();
            for (Attribute attr : Registry.ATTRIBUTE) {
                normal.put(attr.getKey().toString(), attr);
                String name = attr.getKey().getKey();
                String fixed = name.substring(name.lastIndexOf(".") + 1);
                normal.put(fixed, attr);
            }
            YamlDecoder<Attribute> decoder = new LookupDecoder<>(normal);
            return (ctx, yaml) -> {
                var res = decoder.decode(ctx, yaml);
                if (res.hasResult()) return res;
                return YamlDecoder.STRING.decode(ctx, yaml).flatMap(s -> {
                    String fixed = s.substring(s.lastIndexOf(".") + 1)
                            .replace("GENERIC_", "")
                            .replace("HORSE_", "")
                            .replace("ZOMBIE_", "");
                    return decoder.decode(ctx, YamlValue.wrap(fixed));
                });
            };
        });

        private static <T> T make(Supplier<T> s) {
            return s.get();
        }

        public static final YamlDecoder<AttributeModifier> ATTRIBUTE_MODIFIER_DECODER =
                AttributeData.DECODER.map(AttributeData::toAttributeModifier);

        private record AttributeData(UUID uuid, String name, double amount, AttributeModifier.Operation operation,
                                     EquipmentSlot slot) {
            private static final YamlDecoder<AttributeModifier.Operation> OPERATION_DECODER =
                    LookupDecoder.fromEnum(AttributeModifier.Operation.values());
            public static final YamlDecoder<AttributeData> DECODER = RecordYamlDecoder.mapOf(
                    AttributeData::new,
                    YamlDecoder.STRING.flatMap(s -> DataResult.accept(() -> UUID.fromString(s), DataResult::error))
                            .fieldOf("uuid"),
                    YamlDecoder.STRING.fieldOf("name"),
                    YamlDecoder.DOUBLE.fieldOf("amount"),
                    OPERATION_DECODER.fieldOf("operation"),
                    BukkitCodecs.equipment_slot().asDecoder().fieldOf("slot")
            );

            private AttributeData(UUID uuid, String name, double amount, AttributeModifier.Operation operation, EquipmentSlot slot) {
                this.uuid = Objects.requireNonNullElseGet(uuid, UUID::randomUUID);
                this.name = name;
                this.amount = amount;
                this.operation = operation;
                this.slot = slot;
            }

            public AttributeModifier toAttributeModifier() {
                return new AttributeModifier(
                        uuid,
                        name,
                        amount,
                        operation,
                        slot
                );
            }

            public static AttributeData fromAttributeModifier(AttributeModifier modifier) {
                return new AttributeData(
                        modifier.getUniqueId(),
                        modifier.getName(),
                        modifier.getAmount(),
                        modifier.getOperation(),
                        modifier.getSlot()
                );
            }
        }
    }
}
