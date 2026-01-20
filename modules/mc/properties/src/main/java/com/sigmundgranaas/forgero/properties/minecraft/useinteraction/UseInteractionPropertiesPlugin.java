package com.sigmundgranaas.forgero.properties.minecraft.useinteraction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.effects.EffectCodecRegistry;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.effects.entity.ParticleHandler;
import com.sigmundgranaas.forgero.effects.entity.SoundHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.ConsumeStackHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.ConsumeUpgradeHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.CooldownHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.DamageStackHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.StartUseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.ThrowHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.ThrownItemEntityRegistry;

/**
 * Plugin that registers the UseInteraction property system.
 * This includes the property codec and all built-in use phase handlers.
 *
 * <p>This plugin follows the same pattern as {@link com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin}:</p>
 * <ul>
 *   <li>Handlers are registered in a static map with type strings</li>
 *   <li>Property codec is registered via PluginRegistrationContext</li>
 * </ul>
 *
 * <p>Handlers are registered via {@link #registerHandler(String, Codec)} and are
 * available for use in UseInteractionProperty JSON configurations.</p>
 */
public class UseInteractionPropertiesPlugin implements DataPlugin {

	private static final Logger LOGGER = LoggerFactory.getLogger(UseInteractionPropertiesPlugin.class);
	private static final Map<String, Codec<? extends UseHandler>> HANDLERS = new ConcurrentHashMap<>();

	static {
		// Register built-in handlers
		registerHandler(StartUseHandler.TYPE, StartUseHandler.CODEC);
		registerHandler(ConsumeStackHandler.TYPE, ConsumeStackHandler.CODEC);
		registerHandler(ConsumeUpgradeHandler.TYPE, ConsumeUpgradeHandler.CODEC);
		registerHandler(DamageStackHandler.TYPE, DamageStackHandler.CODEC);
		registerHandler(CooldownHandler.TYPE, CooldownHandler.CODEC);
		registerHandler(ThrowHandler.TYPE, ThrowHandler.CODEC);

		// Reuse handlers from OnHit effects system
		registerHandler(SoundHandler.TYPE, SoundHandler.CODEC);
		registerHandler(ParticleHandler.TYPE, ParticleHandler.CODEC);

		// Register entity types
		ThrownItemEntityRegistry.register();
	}

	/**
	 * Registers a new handler type.
	 *
	 * @param type  the handler type identifier (e.g., "forgero:start_use")
	 * @param codec the codec for the handler
	 */
	public static void registerHandler(String type, Codec<? extends UseHandler> codec) {
		if (HANDLERS.containsKey(type)) {
			LOGGER.warn("Overwriting UseHandler registration for type: {}", type);
		}
		HANDLERS.put(type, codec);
		EffectCodecRegistry.registerUseHandler(type, codec);
	}

	/**
	 * Gets the codec for a specific handler type.
	 *
	 * @param type the handler type identifier
	 * @return the codec, or null if not found
	 * @deprecated Use {@link EffectCodecRegistry#getUseHandlerCodec(String)} instead
	 */
	@Deprecated
	public static Codec<? extends UseHandler> getHandlerCodec(String type) {
		return HANDLERS.get(type);
	}

	/**
	 * Checks if a handler type is registered.
	 *
	 * @param type the handler type identifier
	 * @return true if the handler is registered
	 */
	public static boolean isHandlerRegistered(String type) {
		return HANDLERS.containsKey(type);
	}

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerPropertyCodec(
				UseInteractionProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(
						UseInteractionProperty.codec(conditionCodecSupplier.get())
				)
		);
	}

	@Override
	public String getId() {
		return "forgero:use-interaction-properties";
	}
}
