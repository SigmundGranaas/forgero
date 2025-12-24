package com.sigmundgranaas.forgero.properties.minecraft.blockuse.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.useinteraction.BlockUseContext;
import com.sigmundgranaas.forgero.common.useinteraction.BlockUseEffect;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

/**
 * Tills dirt into farmland when used on it.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:till_soil"
 * }
 * </pre>
 */
public record TillSoilEffect() implements BlockUseEffect {
	public static final String TYPE = "forgero:till_soil";

	public static final Codec<TillSoilEffect> CODEC = RecordCodecBuilder.create(instance ->
			instance.point(new TillSoilEffect())
	);

	@Override
	public ActionResult apply(BlockUseContext context) {
		if (context.isClient()) {
			return ActionResult.SUCCESS;
		}

		BlockState state = context.world().getBlockState(context.pos());
		Block block = state.getBlock();

		// Check if it's dirt, grass, or similar tillable blocks
		if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK ||
				block == Blocks.DIRT_PATH || block == Blocks.COARSE_DIRT) {

			// Check if there's air above
			if (context.world().getBlockState(context.pos().up()).isAir()) {
				context.world().setBlockState(context.pos(), Blocks.FARMLAND.getDefaultState());
				context.world().playSound(
						null,
						context.pos(),
						SoundEvents.ITEM_HOE_TILL,
						SoundCategory.BLOCKS,
						1.0f,
						1.0f
				);
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
