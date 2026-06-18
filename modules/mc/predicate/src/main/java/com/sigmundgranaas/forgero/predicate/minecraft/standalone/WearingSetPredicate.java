package com.sigmundgranaas.forgero.predicate.minecraft.standalone;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Dynamic condition: passes when the source entity wears at least {@code min_count} armor pieces
 * whose item belongs to the given item {@code tag}. Powers set bonuses.
 *
 * <pre>{ "type": "forgero:wearing_set", "tag": "forgero:dragon_set", "min_count": 4 }</pre>
 */
public record WearingSetPredicate(String tag, int minCount) implements EvaluableCondition {
	public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "wearing_set");

	public static final Codec<WearingSetPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("tag").forGetter(WearingSetPredicate::tag),
			Codec.INT.optionalFieldOf("min_count", 4).forGetter(WearingSetPredicate::minCount)
	).apply(instance, WearingSetPredicate::new));

	@Override
	public boolean test(DynamicContext context) {
		Optional<Entity> sourceOpt = context.get(MinecraftContextKeys.SOURCE_ENTITY);
		if (sourceOpt.isEmpty() || !(sourceOpt.get() instanceof LivingEntity living)) {
			return false;
		}
		Identifier tagId = Identifier.tryParse(tag);
		if (tagId == null) {
			return false;
		}
		TagKey<net.minecraft.item.Item> tagKey = TagKey.of(RegistryKeys.ITEM, tagId);

		int count = 0;
		for (ItemStack stack : living.getArmorItems()) {
			if (!stack.isEmpty() && stack.isIn(tagKey)) {
				count++;
			}
		}
		return count >= minCount;
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
