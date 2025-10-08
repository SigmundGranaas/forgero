package com.sigmundgranaas.forgero.properties.minecraft.onhit;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnHitPropertiesPlugin implements DataPlugin {

	private static final Map<String, Codec<? extends OnHitHandler>> HANDLERS = new ConcurrentHashMap<>();

	static {
		// Existing handlers
		register(FireHandler.TYPE, FireHandler.CODEC);
		register(ExplosionHandler.TYPE, ExplosionHandler.CODEC);
		register(StatusEffectHandler.TYPE, StatusEffectHandler.CODEC);
		register(KnockbackHandler.TYPE, KnockbackHandler.CODEC);
		register(LifeStealHandler.TYPE, LifeStealHandler.CODEC);
		register(LightningHandler.TYPE, LightningHandler.CODEC);
		register(ConvertHandler.TYPE, ConvertHandler.CODEC);
		register(DisarmHandler.TYPE, DisarmHandler.CODEC);
	}

	public static void register(String type, Codec<? extends OnHitHandler> codec) {
		HANDLERS.put(type, codec);
	}

	public static Codec<? extends OnHitHandler> getHandlerCodec(String type) {
		return HANDLERS.get(type);
	}

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerPropertyCodec(
				OnHitProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnHitProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-hit-properties";
	}
}
