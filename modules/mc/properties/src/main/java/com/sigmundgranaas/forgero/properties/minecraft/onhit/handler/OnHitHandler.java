package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.entity.Entity;

public interface OnHitHandler {
	void onHit(Entity source, Entity target);

	String type();

	static Codec<? extends OnHitHandler> getCodec(String type) {
		Codec<? extends OnHitHandler> codec = OnHitPropertiesPlugin.getHandlerCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown OnHitHandler type: " + type);
		}
		return codec;
	}

	Codec<OnHitHandler> CODEC = DispatchCodecUtils.create(
			OnHitHandler::getCodec,
			OnHitHandler::type
	);
}
