package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.swing.SwingHandPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

/**
 * A marker interface for any effect that can be triggered by hand swing events.
 * Swing events occur when a player swings their hand (attack animation).
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Play sounds on swing</li>
 *   <li>Spawn particles during swing animation</li>
 *   <li>Apply cooldowns</li>
 *   <li>Trigger abilities on swing</li>
 *   <li>Apply effects to nearby entities</li>
 * </ul>
 */
public interface SwingEffect {
	/**
	 * Applies the swing effect.
	 *
	 * @param source The entity that swung their hand
	 * @param hand The hand that was swung
	 */
	void apply(Entity source, Hand hand);

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
	static Codec<? extends SwingEffect> getCodec(String type) {
		Codec<? extends SwingEffect> codec = SwingHandPropertiesPlugin.getEffectCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown SwingEffect type: " + type);
		}
		return codec;
	}

	/**
	 * Dispatch codec for polymorphic SwingEffect deserialization.
	 * Uses the type() method to determine which specific codec to use.
	 */
	Codec<SwingEffect> CODEC = DispatchCodecUtils.create(
			SwingEffect::getCodec,
			SwingEffect::type
	);
}
