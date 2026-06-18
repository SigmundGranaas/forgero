package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;
import com.sigmundgranaas.forgero.effects.mark.MarkStore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Dynamic condition: passes when the target entity carries the given mark. Mirrors
 * {@code forgero:target_has_tag}, but for transient marks rather than static tags.
 */
public record TargetHasMarkCondition(Identifier mark) implements EvaluableCondition {
	public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "target_has_mark");

	public static final Codec<TargetHasMarkCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("mark").forGetter(TargetHasMarkCondition::mark)
	).apply(instance, TargetHasMarkCondition::new));

	@Override
	public boolean test(DynamicContext context) {
		return context.get(MinecraftContextKeys.TARGET_ENTITY)
				.filter(entity -> entity instanceof LivingEntity)
				.map(entity -> MarkStore.hasMark((LivingEntity) entity, mark))
				.orElse(false);
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
