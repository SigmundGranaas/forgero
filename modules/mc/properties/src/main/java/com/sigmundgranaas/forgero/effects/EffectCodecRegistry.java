package com.sigmundgranaas.forgero.effects;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.effects.block.OnHitBlockEffect;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for effect codecs.
 *
 * <p>This registry decouples effect interfaces from plugin classes,
 * allowing plugins to register codecs without creating circular dependencies.</p>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Plugins call register methods during initialization</li>
 *   <li>Effect interfaces call get methods for codec dispatch</li>
 * </ul>
 */
public final class EffectCodecRegistry {

	private static final Map<String, Codec<? extends OnHitEffect>> ON_HIT_EFFECTS = new ConcurrentHashMap<>();
	private static final Map<String, Codec<? extends OnHitBlockEffect>> ON_HIT_BLOCK_EFFECTS = new ConcurrentHashMap<>();
	private static final Map<String, Codec<? extends UseHandler>> USE_HANDLERS = new ConcurrentHashMap<>();

	private EffectCodecRegistry() {}

	// OnHitEffect registration
	public static void registerOnHitEffect(String type, Codec<? extends OnHitEffect> codec) {
		ON_HIT_EFFECTS.put(type, codec);
	}

	public static Codec<? extends OnHitEffect> getOnHitEffectCodec(String type) {
		return ON_HIT_EFFECTS.get(type);
	}

	// OnHitBlockEffect registration
	public static void registerOnHitBlockEffect(String type, Codec<? extends OnHitBlockEffect> codec) {
		ON_HIT_BLOCK_EFFECTS.put(type, codec);
	}

	public static Codec<? extends OnHitBlockEffect> getOnHitBlockEffectCodec(String type) {
		return ON_HIT_BLOCK_EFFECTS.get(type);
	}

	// UseHandler registration
	public static void registerUseHandler(String type, Codec<? extends UseHandler> codec) {
		USE_HANDLERS.put(type, codec);
	}

	public static Codec<? extends UseHandler> getUseHandlerCodec(String type) {
		return USE_HANDLERS.get(type);
	}
}
