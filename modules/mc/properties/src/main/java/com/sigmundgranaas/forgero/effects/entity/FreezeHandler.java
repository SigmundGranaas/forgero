package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * Applies a freeze effect to the target entity by setting frozen ticks.
 * This creates an ice overlay effect and can optionally be purely visual.
 *
 * <h3>JSON Configuration Examples:</h3>
 *
 * <p><b>Full Freeze (Damage + Slowness):</b></p>
 * <pre>
 * {
 *   "type": "forgero:freeze",
 *   "duration": 100,
 *   "visual_only": false
 * }
 * </pre>
 *
 * <p><b>Visual Ice Overlay Only:</b></p>
 * <pre>
 * {
 *   "type": "forgero:freeze",
 *   "duration": 60,
 *   "visual_only": true
 * }
 * </pre>
 *
 * <h3>Freeze Mechanics:</h3>
 * <ul>
 *   <li><b>Normal Mode:</b> Full freeze with damage, slowness, and visual overlay</li>
 *   <li><b>Visual Only Mode:</b> Ice overlay effect without freeze damage or slowness</li>
 *   <li>Duration in ticks (20 ticks = 1 second)</li>
 *   <li>Minimum freeze for visual effect: ~40 ticks</li>
 * </ul>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Ice/Snow themed weapons - freeze enemies on hit</li>
 *   <li>Crowd control - slow and freeze mobs</li>
 *   <li>Visual-only mode for cosmetic ice overlay without gameplay impact</li>
 *   <li>Stacking freeze effects for longer duration</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * This handler is lightweight and uses Minecraft's built-in freeze mechanic.
 * Visual-only mode uses NBT tracking to prevent freeze damage.
 *
 * @param duration The freeze duration in ticks (20 ticks = 1 second, recommended: 40-200)
 * @param visualOnly If true, applies visual ice overlay without freeze damage/slowness
 */
public record FreezeHandler(int duration, boolean visualOnly) implements EntityEffectHandler {
	public static final String TYPE = "forgero:freeze";
	private static final String VISUAL_ONLY_NBT_KEY = "forgero:freeze_visual_only";

	public static final Codec<FreezeHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(1, 600).fieldOf("duration").forGetter(FreezeHandler::duration),
			Codec.BOOL.optionalFieldOf("visual_only", false).forGetter(FreezeHandler::visualOnly)
	).apply(instance, FreezeHandler::new));

	@Override
	public void apply(Entity entity) {
		// Apply freeze ticks for visual ice overlay
		entity.setFrozenTicks(duration);

		// If visual-only mode, mark entity to prevent freeze damage
		if (visualOnly && entity instanceof LivingEntity) {
			NbtCompound nbt = new NbtCompound();
			entity.writeNbt(nbt);
			nbt.putBoolean(VISUAL_ONLY_NBT_KEY, true);
			nbt.putInt(VISUAL_ONLY_NBT_KEY + "_expires", entity.age + duration);
			entity.readNbt(nbt);
		}
	}

	/**
	 * Checks if an entity has visual-only freeze active.
	 * This can be used by mixins to prevent freeze damage.
	 *
	 * @param entity The entity to check
	 * @return true if entity has active visual-only freeze
	 */
	public static boolean hasVisualOnlyFreeze(Entity entity) {
		NbtCompound nbt = new NbtCompound();
		entity.writeNbt(nbt);

		if (!nbt.getBoolean(VISUAL_ONLY_NBT_KEY)) {
			return false;
		}

		// Check if freeze has expired
		int expiresAt = nbt.getInt(VISUAL_ONLY_NBT_KEY + "_expires");
		if (entity.age >= expiresAt) {
			// Clean up expired freeze marker
			nbt.remove(VISUAL_ONLY_NBT_KEY);
			nbt.remove(VISUAL_ONLY_NBT_KEY + "_expires");
			entity.readNbt(nbt);
			return false;
		}

		return true;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
