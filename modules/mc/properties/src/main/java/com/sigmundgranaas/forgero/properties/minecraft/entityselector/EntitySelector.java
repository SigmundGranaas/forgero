package com.sigmundgranaas.forgero.properties.minecraft.entityselector;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;
import net.minecraft.entity.Entity;

import java.util.List;

/**
 * Defines "who" or "what" to affect in an on-hit event.
 * An EntitySelector takes the initial context of an event (the attacker and the direct target)
 * and produces a list of final targets for effects to be applied to.
 */
public interface EntitySelector {
	List<Entity> select(Entity source, Entity initialTarget);

	String type();

	static Codec<? extends EntitySelector> getCodec(String type) {
		Codec<? extends EntitySelector> codec = OnHitPropertiesPlugin.getSelectorCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown EntitySelector type: " + type);
		}
		return codec;
	}

	Codec<EntitySelector> CODEC = DispatchCodecUtils.create(
			EntitySelector::getCodec,
			EntitySelector::type
	);
}
