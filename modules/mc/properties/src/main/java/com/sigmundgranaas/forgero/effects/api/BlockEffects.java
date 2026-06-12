package com.sigmundgranaas.forgero.effects.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.effects.EffectCodecRegistry;
import com.sigmundgranaas.forgero.effects.block.OnHitBlockEffect;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Public entry point for adding custom <em>block</em> effects — the "what happens to the block/world
 * when a tool hits or breaks a block" channel (used by {@code block_breaking} / on-hit-block
 * properties). Mirrors {@link OnHitEffects}: supply plain Minecraft logic over
 * {@code (World, Entity source, BlockPos)} and, optionally, a {@link Codec} for JSON config; the
 * internal {@code OnHitBlockEffect} marker, {@code type()} boilerplate and {@code EffectCodecRegistry}
 * are hidden.
 *
 * <pre>{@code
 * BlockEffects.register("mymod:scorch", (world, source, pos) -> world.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState()));
 * }</pre>
 */
public final class BlockEffects {

	private BlockEffects() {
	}

	/** Block effect logic with no JSON config. */
	@FunctionalInterface
	public interface Action {
		void apply(World world, Entity source, BlockPos pos);
	}

	/** Block effect logic that reads parsed JSON config {@code C}. */
	@FunctionalInterface
	public interface ConfigAction<C> {
		void apply(C config, World world, Entity source, BlockPos pos);
	}

	/** Registers a block effect with no JSON config. */
	public static void register(String type, Action action) {
		OnHitBlockEffect effect = new OnHitBlockEffect() {
			@Override
			public void apply(World world, Entity source, BlockPos pos) {
				action.apply(world, source, pos);
			}

			@Override
			public String type() {
				return type;
			}
		};
		EffectCodecRegistry.registerOnHitBlockEffect(type, Codec.unit(effect));
	}

	/** Registers a block effect that parses JSON config. */
	public static <C> void register(String type, Codec<C> configCodec, ConfigAction<C> action) {
		Codec<Holder<C>> codec = configCodec.xmap(
				config -> new Holder<>(type, config, action),
				Holder::config);
		EffectCodecRegistry.registerOnHitBlockEffect(type, codec);
	}

	private record Holder<C>(String type, C config, ConfigAction<C> action) implements OnHitBlockEffect {
		@Override
		public void apply(World world, Entity source, BlockPos pos) {
			action.apply(config, world, source, pos);
		}
	}
}
