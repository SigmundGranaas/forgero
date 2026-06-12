package com.sigmundgranaas.forgero.effects.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.effects.EffectCodecRegistry;
import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import net.minecraft.entity.Entity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Public entry point for adding custom on-hit effects. An addon supplies plain Minecraft logic
 * (a {@link Consumer}/{@link BiConsumer} over {@code Entity}) and, optionally, a {@link Codec} for
 * JSON config — and nothing else. The {@code OnHitEffect} marker, the {@code type()} boilerplate and
 * the internal {@code EffectCodecRegistry} are all hidden.
 *
 * <p>Once registered the effect is usable in content JSON inside any entity event's {@code effects}
 * list (on-hit, on-tick, …):
 * <pre>{@code
 * { "type": "mymod:zap", "damage": 4.0 }
 * }</pre>
 *
 * <p>Call these from your mod's init (a {@code ModInitializer} or a Forgero data plugin), before
 * content loads.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // No config — set the victim on fire for 3s:
 * OnHitEffects.registerSingleTarget("mymod:torch", e -> e.setOnFireFor(3));
 *
 * // JSON config — heal the attacker by N:
 * record Heal(float amount) {}
 * Codec<Heal> codec = RecordCodecBuilder.create(i -> i.group(
 *         Codec.FLOAT.fieldOf("amount").forGetter(Heal::amount)).apply(i, Heal::new));
 * OnHitEffects.registerSourceTarget("mymod:lifesteal", codec, (cfg, source, target) -> {
 *     if (source instanceof LivingEntity le) le.heal(cfg.amount());
 * });
 * }</pre>
 */
public final class OnHitEffects {

	private OnHitEffects() {
	}

	/** Registers a single-target effect with no JSON config. */
	public static void registerSingleTarget(String type, Consumer<Entity> action) {
		EntityEffectHandler handler = new EntityEffectHandler() {
			@Override
			public void apply(Entity entity) {
				action.accept(entity);
			}

			@Override
			public String type() {
				return type;
			}
		};
		EffectCodecRegistry.registerOnHitEffect(type, Codec.unit(handler));
	}

	/** Registers a single-target effect that parses JSON config. */
	public static <C> void registerSingleTarget(String type, Codec<C> configCodec, BiConsumer<C, Entity> action) {
		Codec<FunctionalEntityEffect<C>> codec = configCodec.xmap(
				config -> new FunctionalEntityEffect<>(type, config, action),
				FunctionalEntityEffect::config);
		EffectCodecRegistry.registerOnHitEffect(type, codec);
	}

	/** Registers a source+target effect with no JSON config. */
	public static void registerSourceTarget(String type, BiConsumer<Entity, Entity> action) {
		ContextualEffectHandler handler = new ContextualEffectHandler() {
			@Override
			public void apply(Entity source, Entity target) {
				action.accept(source, target);
			}

			@Override
			public String type() {
				return type;
			}
		};
		EffectCodecRegistry.registerOnHitEffect(type, Codec.unit(handler));
	}

	/** Registers a source+target effect that parses JSON config. */
	public static <C> void registerSourceTarget(String type, Codec<C> configCodec, SourceTargetAction<C> action) {
		Codec<FunctionalContextualEffect<C>> codec = configCodec.xmap(
				config -> new FunctionalContextualEffect<>(type, config, action),
				FunctionalContextualEffect::config);
		EffectCodecRegistry.registerOnHitEffect(type, codec);
	}
}
