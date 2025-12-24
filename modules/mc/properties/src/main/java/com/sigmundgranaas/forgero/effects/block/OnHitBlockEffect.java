package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.onhitblock.OnHitBlockPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A marker interface for any effect that can be triggered when an entity hits a block.
 * Unlike OnHitEffect which targets entities, OnHitBlockEffect operates when tools/weapons
 * strike blocks (not breaking them, but hitting them).
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Apply effects to the hit block (transformation, damage)</li>
 *   <li>Apply effects to nearby blocks</li>
 *   <li>Spawn particles/sounds at block hit location</li>
 *   <li>Trigger effects on the entity that hit the block</li>
 *   <li>Harvest effects (auto-collection, bonus drops)</li>
 * </ul>
 */
public interface OnHitBlockEffect {
	/**
	 * Applies the block hit effect at the specified position.
	 *
	 * @param world The world where the block was hit
	 * @param source The entity that hit the block
	 * @param pos The position of the block that was hit
	 */
	void apply(World world, Entity source, BlockPos pos);

	/**
	 * Returns the type identifier for this effect.
	 * Used for codec dispatch and serialization.
	 */
	String type();

	/**
	 * Retrieves the codec for a specific effect type from the plugin registry.
	 *
	 * @param type The effect type identifier
	 * @return The codec for deserializing this effect type
	 * @throws IllegalArgumentException if the type is not registered
	 */
	static Codec<? extends OnHitBlockEffect> getCodec(String type) {
		Codec<? extends OnHitBlockEffect> codec = OnHitBlockPropertiesPlugin.getEffectCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown OnHitBlockEffect type: " + type);
		}
		return codec;
	}

	/**
	 * Dispatch codec for polymorphic OnHitBlockEffect deserialization.
	 * Uses the type() method to determine which specific codec to use.
	 */
	Codec<OnHitBlockEffect> CODEC = DispatchCodecUtils.create(
			OnHitBlockEffect::getCodec,
			OnHitBlockEffect::type
	);
}
