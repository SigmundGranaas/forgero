package com.sigmundgranaas.forgero.properties.minecraft.swing;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.effects.entity.SwingEffect;
import com.sigmundgranaas.forgero.effects.entity.SwingParticleEffect;
import com.sigmundgranaas.forgero.effects.entity.SwingSoundEffect;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin for registering SwingHand properties and their effects.
 * This plugin manages the codec registry for all swing effects.
 */
public class SwingHandPropertiesPlugin implements DataPlugin {

	private static final Map<String, Codec<? extends SwingEffect>> EFFECTS = new ConcurrentHashMap<>();

	static {
		// Register swing effects
		registerEffect(SwingSoundEffect.TYPE, SwingSoundEffect.CODEC);
		registerEffect(SwingParticleEffect.TYPE, SwingParticleEffect.CODEC);
	}

	public static void registerEffect(String type, Codec<? extends SwingEffect> codec) {
		EFFECTS.put(type, codec);
	}

	public static Codec<? extends SwingEffect> getEffectCodec(String type) {
		return EFFECTS.get(type);
	}

	@Override
	public void register(PluginRegistrationContext context) {
		// Register the compile pass so terminals pre-compile this property at construction.
		CompilerPasses.register(SwingHandProperty.KEY, SwingHandProperty.Engine::new);
		context.registerPropertyCodec(
				SwingHandProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(SwingHandProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:swing-hand-properties";
	}
}
