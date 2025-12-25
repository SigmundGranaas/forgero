package com.sigmundgranaas.forgero.properties.minecraft.onhitblock;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.effects.EffectCodecRegistry;
import com.sigmundgranaas.forgero.effects.block.BlockParticleEffect;
import com.sigmundgranaas.forgero.effects.block.BlockSoundEffect;
import com.sigmundgranaas.forgero.effects.block.IgniteBlockEffect;
import com.sigmundgranaas.forgero.effects.block.OnHitBlockEffect;
import com.sigmundgranaas.forgero.effects.block.PlaceBlockEffect;
import com.sigmundgranaas.forgero.effects.block.TransformBlockEffect;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin for registering OnHitBlock properties and their effects.
 * This plugin manages the codec registry for all block hit effects.
 */
public class OnHitBlockPropertiesPlugin implements DataPlugin {

	private static final Map<String, Codec<? extends OnHitBlockEffect>> EFFECTS = new ConcurrentHashMap<>();

	static {
		// Register block hit effects
		registerEffect(BlockSoundEffect.TYPE, BlockSoundEffect.CODEC);
		registerEffect(BlockParticleEffect.TYPE, BlockParticleEffect.CODEC);
		registerEffect(TransformBlockEffect.TYPE, TransformBlockEffect.CODEC);
		registerEffect(PlaceBlockEffect.TYPE, PlaceBlockEffect.CODEC);
		registerEffect(IgniteBlockEffect.TYPE, IgniteBlockEffect.CODEC);
	}

	public static void registerEffect(String type, Codec<? extends OnHitBlockEffect> codec) {
		EFFECTS.put(type, codec);
		EffectCodecRegistry.registerOnHitBlockEffect(type, codec);
	}

	/**
	 * @deprecated Use {@link EffectCodecRegistry#getOnHitBlockEffectCodec(String)} instead
	 */
	@Deprecated
	public static Codec<? extends OnHitBlockEffect> getEffectCodec(String type) {
		return EFFECTS.get(type);
	}

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerPropertyCodec(
				OnHitBlockProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnHitBlockProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-hit-block-properties";
	}
}
