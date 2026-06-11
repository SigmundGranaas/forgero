package com.sigmundgranaas.forgero.properties.minecraft.onblockplace;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.effects.block.BlockEffect;
import com.sigmundgranaas.forgero.effects.block.BlockSelector;
import com.sigmundgranaas.forgero.effects.block.PlacedBlockSelector;
import com.sigmundgranaas.forgero.effects.block.RadiusBlockSelector;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;

import java.util.HashMap;
import java.util.Map;

/**
 * Plugin for registering On-Block-Place properties with the Forgero system.
 * This enables block-based effects that trigger when the wielder places a block.
 *
 * <h3>Registered Components:</h3>
 * <ul>
 *   <li>OnBlockPlaceProperty - The main property type</li>
 *   <li>BlockSelectors - Determine which positions are affected</li>
 *   <li>BlockEffects - Define what happens at selected positions</li>
 * </ul>
 */
public class OnBlockPlacePropertiesPlugin implements DataPlugin {
	private static final Map<String, Codec<? extends BlockEffect>> effectCodecs = new HashMap<>();
	private static final Map<String, Codec<? extends BlockSelector>> selectorCodecs = new HashMap<>();

	static {
		// Register built-in selectors
		registerSelector(PlacedBlockSelector.TYPE, PlacedBlockSelector.CODEC);
		registerSelector(RadiusBlockSelector.TYPE, RadiusBlockSelector.CODEC);

		// BlockEffects will be registered by their respective implementations or plugins
	}

	@Override
	public void register(PluginRegistrationContext context) {
		// Register the compile pass so terminals pre-compile this property at construction.
		CompilerPasses.register(OnBlockPlaceProperty.KEY, OnBlockPlaceProperty.Engine::new);
		context.registerPropertyCodec(
				OnBlockPlaceProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnBlockPlaceProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-block-place-properties";
	}

	/**
	 * Registers a BlockEffect codec for use in deserialization.
	 *
	 * @param type The effect type identifier
	 * @param codec The codec for this effect type
	 */
	public static void registerEffect(String type, Codec<? extends BlockEffect> codec) {
		effectCodecs.put(type, codec);
	}

	/**
	 * Retrieves a registered BlockEffect codec.
	 *
	 * @param type The effect type identifier
	 * @return The codec, or null if not registered
	 */
	public static Codec<? extends BlockEffect> getEffectCodec(String type) {
		return effectCodecs.get(type);
	}

	/**
	 * Registers a BlockSelector codec for use in deserialization.
	 *
	 * @param type The selector type identifier
	 * @param codec The codec for this selector type
	 */
	public static void registerSelector(String type, Codec<? extends BlockSelector> codec) {
		selectorCodecs.put(type, codec);
	}

	/**
	 * Retrieves a registered BlockSelector codec.
	 *
	 * @param type The selector type identifier
	 * @return The codec, or null if not registered
	 */
	public static Codec<? extends BlockSelector> getSelectorCodec(String type) {
		return selectorCodecs.get(type);
	}
}
