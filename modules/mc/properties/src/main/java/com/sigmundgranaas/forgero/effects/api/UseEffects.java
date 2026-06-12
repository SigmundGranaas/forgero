package com.sigmundgranaas.forgero.effects.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.effects.EffectCodecRegistry;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Public entry point for adding custom <em>use</em> effects — what happens when a player uses
 * (right-clicks with) a Forgero item. Mirrors {@link OnHitEffects}: supply plain Minecraft logic
 * over {@code (LivingEntity user, ItemStack stack, Hand hand)} and, optionally, a {@link Codec} for
 * JSON config; the internal {@code SimpleUseHandler} type, {@code type()} boilerplate and
 * {@code EffectCodecRegistry} are hidden.
 *
 * <pre>{@code
 * UseEffects.register("mymod:feed", (user, stack, hand) -> user.heal(4.0f));
 * }</pre>
 */
public final class UseEffects {

	private UseEffects() {
	}

	/** Use effect logic with no JSON config. */
	@FunctionalInterface
	public interface Action {
		void apply(LivingEntity user, ItemStack stack, Hand hand);
	}

	/** Use effect logic that reads parsed JSON config {@code C}. */
	@FunctionalInterface
	public interface ConfigAction<C> {
		void apply(C config, LivingEntity user, ItemStack stack, Hand hand);
	}

	/** Registers a use effect with no JSON config. */
	public static void register(String type, Action action) {
		SimpleUseHandler handler = new SimpleUseHandler() {
			@Override
			public void apply(LivingEntity user, ItemStack stack, Hand hand) {
				action.apply(user, stack, hand);
			}

			@Override
			public String type() {
				return type;
			}
		};
		EffectCodecRegistry.registerUseHandler(type, Codec.unit(handler));
	}

	/** Registers a use effect that parses JSON config. */
	public static <C> void register(String type, Codec<C> configCodec, ConfigAction<C> action) {
		Codec<Holder<C>> codec = configCodec.xmap(
				config -> new Holder<>(type, config, action),
				Holder::config);
		EffectCodecRegistry.registerUseHandler(type, codec);
	}

	private record Holder<C>(String type, C config, ConfigAction<C> action) implements SimpleUseHandler {
		@Override
		public void apply(LivingEntity user, ItemStack stack, Hand hand) {
			action.apply(config, user, stack, hand);
		}
	}
}
