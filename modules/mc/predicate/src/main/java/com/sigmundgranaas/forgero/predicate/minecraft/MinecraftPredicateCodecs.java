package com.sigmundgranaas.forgero.predicate.minecraft;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;

import com.sigmundgranaas.forgero.predicate.minecraft.block.BlockPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.entity.EntityPredicate;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a map of all Minecraft-specific condition codecs.
 * This class acts as a DI hub, replacing the old static registries.
 */
public class MinecraftPredicateCodecs {

	public Map<OpenIdentifier, Codec<? extends DynamicCondition>> getDynamicCodecs() {
		Map<OpenIdentifier, Codec<? extends DynamicCondition>> codecs = new HashMap<>();

		codecs.put(BlockPredicate.TYPE, BlockPredicate.CODEC);
		codecs.put(EntityPredicate.TYPE, EntityPredicate.CODEC);

		return codecs;
	}
}
