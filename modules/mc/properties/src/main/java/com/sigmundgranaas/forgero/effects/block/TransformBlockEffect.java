package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Transforms a block into another block type when hit.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:transform_block",
 *   "target_block": "minecraft:stone",
 *   "replace_with": "minecraft:cobblestone"
 * }
 * </pre>
 */
public record TransformBlockEffect(
		String targetBlock,
		String replaceWith
) implements OnHitBlockEffect {
	public static final String TYPE = "forgero:transform_block";

	public static final Codec<TransformBlockEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("target_block", "*").forGetter(TransformBlockEffect::targetBlock),
			Codec.STRING.fieldOf("replace_with").forGetter(TransformBlockEffect::replaceWith)
	).apply(instance, TransformBlockEffect::new));

	@Override
	public void apply(World world, Entity source, BlockPos pos) {
		if (world.isClient()) {
			return;
		}

		BlockState currentState = world.getBlockState(pos);

		// Check if we should transform this block
		if (!targetBlock.equals("*")) {
			Identifier targetId = new Identifier(targetBlock);
			Block target = Registries.BLOCK.get(targetId);
			if (currentState.getBlock() != target) {
				return;
			}
		}

		// Get the replacement block
		Identifier replaceId = new Identifier(replaceWith);
		Block replacement = Registries.BLOCK.get(replaceId);

		if (replacement != null) {
			world.setBlockState(pos, replacement.getDefaultState());
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
