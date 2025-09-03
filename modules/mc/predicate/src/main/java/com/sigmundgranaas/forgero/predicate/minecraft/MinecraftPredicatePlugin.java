package com.sigmundgranaas.forgero.predicate.minecraft;

import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.predicate.minecraft.block.BlockPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.entity.EntityPredicate;

/**
 * A Forgero data plugin that registers all the default Minecraft-specific dynamic conditions (predicates).
 * This makes predicates like entity and block matching available to be used in property conditions.
 */
public class MinecraftPredicatePlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		// Register all Minecraft-specific dynamic condition codecs.
		// These predicates are evaluated at runtime based on the game context.
		context.registerDynamicConditionCodec(BlockPredicate.TYPE.toString(), BlockPredicate.CODEC);
		context.registerDynamicConditionCodec(EntityPredicate.TYPE.toString(), EntityPredicate.CODEC);
	}

	@Override
	public String getId() {
		return "forgero-minecraft-predicates";
	}
}
