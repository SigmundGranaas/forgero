package com.sigmundgranaas.forgero.properties.minecraft.blockuse;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.useinteraction.BlockUseEffect;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;
import com.sigmundgranaas.forgero.properties.minecraft.blockuse.effects.TillSoilEffect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin for registering BlockUse properties and their effects.
 * This plugin manages the codec registry for all block use effects.
 */
public class BlockUsePropertiesPlugin implements DataPlugin {

	private static final Map<String, Codec<? extends BlockUseEffect>> EFFECTS = new ConcurrentHashMap<>();

	static {
		// Register block use effects
		registerEffect(TillSoilEffect.TYPE, TillSoilEffect.CODEC);
	}

	public static void registerEffect(String type, Codec<? extends BlockUseEffect> codec) {
		EFFECTS.put(type, codec);
		// Also register in the interface's static map
		BlockUseEffect.EFFECT_CODECS.put(type, codec);
	}

	public static Codec<? extends BlockUseEffect> getEffectCodec(String type) {
		return EFFECTS.get(type);
	}

	@Override
	public void register(PluginRegistrationContext context) {
		// Register the compile pass so terminals pre-compile this property at construction.
		CompilerPasses.register(BlockUseProperty.KEY, BlockUseProperty.Engine::new);
		context.registerPropertyCodec(
				BlockUseProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(BlockUseProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:block-use-properties";
	}
}
