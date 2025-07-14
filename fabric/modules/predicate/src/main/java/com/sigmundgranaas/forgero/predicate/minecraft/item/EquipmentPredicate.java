package com.sigmundgranaas.forgero.predicate.minecraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;

/**
 * Predicate for a living entity's equipped items.
 *
 * <p><h3>Example:</h3>
 * <pre>
 * "equipment": {
 *   "mainhand": { "tag": "forgero:hammers" },
 *   "head": { "item": "minecraft:iron_helmet" }
 * }
 * </pre>
 */
public record EquipmentPredicate(
		Optional<ItemPredicate> mainhand,
		Optional<ItemPredicate> offhand,
		Optional<ItemPredicate> head,
		Optional<ItemPredicate> chest,
		Optional<ItemPredicate> legs,
		Optional<ItemPredicate> feet
) {
	public static final Codec<EquipmentPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ItemPredicate.CODEC.optionalFieldOf("mainhand").forGetter(EquipmentPredicate::mainhand),
					ItemPredicate.CODEC.optionalFieldOf("offhand").forGetter(EquipmentPredicate::offhand),
					ItemPredicate.CODEC.optionalFieldOf("head").forGetter(EquipmentPredicate::head),
					ItemPredicate.CODEC.optionalFieldOf("chest").forGetter(EquipmentPredicate::chest),
					ItemPredicate.CODEC.optionalFieldOf("legs").forGetter(EquipmentPredicate::legs),
					ItemPredicate.CODEC.optionalFieldOf("feet").forGetter(EquipmentPredicate::feet)
			).apply(instance, EquipmentPredicate::new)
	);

	public boolean test(LivingEntity entity) {
		boolean mainhandMatch = mainhand.map(p -> p.test(entity.getEquippedStack(EquipmentSlot.MAINHAND))).orElse(true);
		boolean offhandMatch = offhand.map(p -> p.test(entity.getEquippedStack(EquipmentSlot.OFFHAND))).orElse(true);
		boolean headMatch = head.map(p -> p.test(entity.getEquippedStack(EquipmentSlot.HEAD))).orElse(true);
		boolean chestMatch = chest.map(p -> p.test(entity.getEquippedStack(EquipmentSlot.CHEST))).orElse(true);
		boolean legsMatch = legs.map(p -> p.test(entity.getEquippedStack(EquipmentSlot.LEGS))).orElse(true);
		boolean feetMatch = feet.map(p -> p.test(entity.getEquippedStack(EquipmentSlot.FEET))).orElse(true);

		return mainhandMatch && offhandMatch && headMatch && chestMatch && legsMatch && feetMatch;
	}
}
