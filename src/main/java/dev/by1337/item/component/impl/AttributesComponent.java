package dev.by1337.item.component.impl;

import dev.by1337.item.component.MergeableComponent;
import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.YamlValue;
import dev.by1337.yaml.codec.DataResult;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.codec.schema.SchemaType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public record AttributesComponent(List<Entry> modifiers) implements MergeableComponent<AttributesComponent> {
    public static YamlCodec<AttributesComponent> CODEC = Entry.CODEC.listOf()
            .map(AttributesComponent::new, AttributesComponent::modifiers);

    public record Entry(Attribute attribute, AttributeModifier modifier) {
        public static final YamlCodec<Entry> CODEC = RecordYamlCodecBuilder.mapOf(
                Entry::new,
                Codecs.ATTRIBUTE_CODEC.fieldOf("attribute", Entry::attribute),
                Codecs.ATTRIBUTE_MODIFIER_CODEC.fieldOf(null, Entry::modifier)
        );
    }

    @Override
    public AttributesComponent and(AttributesComponent t1) {
        List<Entry> modifiers = new ArrayList<>(this.modifiers);
        modifiers.addAll(t1.modifiers);
        return new AttributesComponent(modifiers);
    }

    private static class Codecs {
        private static final YamlCodec<Attribute> ATTRIBUTE_CODEC = make(() -> {
            Map<String, Attribute> normal = new HashMap<>();
            for (Attribute attr : Registry.ATTRIBUTE) {
                normal.put(attr.getKey().toString(), attr);
                String name = attr.getKey().getKey();
                String fixed = name.substring(name.lastIndexOf(".") + 1);
                normal.put(fixed, attr);
            }
            YamlCodec<Attribute> codec = YamlCodec.lookup(normal);
            return new YamlCodec<Attribute>() {
                @Override
                public DataResult<Attribute> decode(YamlValue yaml) {
                    var res = codec.decode(yaml);
                    if (res.hasResult()) return res;
                    return STRING.decode(yaml).flatMap(s -> {
                        String fixed = s.substring(s.lastIndexOf(".") + 1)
                                .replace("GENERIC_", "")
                                .replace("HORSE_", "")
                                .replace("ZOMBIE_", "")
                                ;
                        return codec.decode(fixed);
                    });
                }

                @Override
                public YamlValue encode(Attribute attribute) {
                    return codec.encode(attribute);
                }

                @Override
                public @NotNull SchemaType schema() {
                    return codec.schema();
                }
            };
        });

        private static <T> T make(Supplier<T> s) {
            return s.get();
        }

        public static final YamlCodec<AttributeModifier> ATTRIBUTE_MODIFIER_CODEC = AttributeData.CODEC
                .map(AttributeData::toAttributeModifier, AttributeData::fromAttributeModifier);

        private record AttributeData(UUID uuid, String name, double amount, AttributeModifier.Operation operation,
                                     EquipmentSlot slot) {
            private static final YamlCodec<AttributeModifier.Operation> OPERATION_CODEC = YamlCodec.fromEnum(AttributeModifier.Operation.class);
            public static final YamlCodec<AttributeData> CODEC = RecordYamlCodecBuilder.mapOf(
                    AttributeData::new,
                    YamlCodec.STRING.map(UUID::fromString, UUID::toString).fieldOf("uuid", AttributeData::uuid),
                    YamlCodec.STRING.fieldOf("name", AttributeData::name),
                    YamlCodec.DOUBLE.fieldOf("amount", AttributeData::amount),
                    OPERATION_CODEC.fieldOf("operation", AttributeData::operation),
                    BukkitCodecs.equipment_slot().fieldOf("slot", AttributeData::slot)
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
