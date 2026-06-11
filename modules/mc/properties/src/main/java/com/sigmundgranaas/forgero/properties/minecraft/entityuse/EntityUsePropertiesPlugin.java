package com.sigmundgranaas.forgero.properties.minecraft.entityuse;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.useinteraction.EntityUseEffect;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;
import com.sigmundgranaas.forgero.properties.minecraft.entityuse.effects.HealEntityEffect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin for registering EntityUse properties and their effects.
 * This plugin manages the codec registry for all entity use effects.
 */
public class EntityUsePropertiesPlugin implements DataPlugin {

	private static final Map<String, Codec<? extends EntityUseEffect>> EFFECTS = new ConcurrentHashMap<>();

	static {
		// Register entity use effects
		registerEffect(HealEntityEffect.TYPE, HealEntityEffect.CODEC);
	}

	public static void registerEffect(String type, Codec<? extends EntityUseEffect> codec) {
		EFFECTS.put(type, codec);
		// Also register in the interface's static map
		EntityUseEffect.EFFECT_CODECS.put(type, codec);
	}

	public static Codec<? extends EntityUseEffect> getEffectCodec(String type) {
		return EFFECTS.get(type);
	}

	@Override
	public void register(PluginRegistrationContext context) {
		// Register the compile pass so terminals pre-compile this property at construction.
		CompilerPasses.register(EntityUseProperty.KEY, EntityUseProperty.Engine::new);
		context.registerPropertyCodec(
				EntityUseProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(EntityUseProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:entity-use-properties";
	}
}
