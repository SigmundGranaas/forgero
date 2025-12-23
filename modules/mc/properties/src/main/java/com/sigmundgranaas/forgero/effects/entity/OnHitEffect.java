package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

/**
 * A marker interface for any effect that can be triggered by an on-hit event.
 * This allows different types of handlers (e.g., those needing context and those that don't)
 * to coexist in the same list.
 */
public interface OnHitEffect {
	String type();

	static Codec<? extends OnHitEffect> getCodec(String type) {
		Codec<? extends OnHitEffect> codec = OnHitPropertiesPlugin.getEffectCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown OnHitEffect type: " + type);
		}
		return codec;
	}

	Codec<OnHitEffect> CODEC = DispatchCodecUtils.create(
			OnHitEffect::getCodec,
			OnHitEffect::type
	);
}
