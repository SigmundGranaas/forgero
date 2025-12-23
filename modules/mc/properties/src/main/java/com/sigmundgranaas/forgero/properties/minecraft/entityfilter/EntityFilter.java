package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;
import net.minecraft.entity.Entity;

/**
 * Filters entities based on various criteria.
 * Filters are applied after selection to narrow down the final list of targets.
 * <p>
 * Filters are composable and reusable across different selectors.
 */
public interface EntityFilter {
	/**
	 * Tests whether the given entity passes this filter.
	 *
	 * @param source    The entity causing the effect (e.g., the attacker)
	 * @param candidate The entity being tested
	 * @return true if the entity passes the filter, false otherwise
	 */
	boolean test(Entity source, Entity candidate);

	String type();

	static Codec<? extends EntityFilter> getCodec(String type) {
		Codec<? extends EntityFilter> codec = OnHitPropertiesPlugin.getFilterCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown EntityFilter type: " + type);
		}
		return codec;
	}

	Codec<EntityFilter> CODEC = DispatchCodecUtils.create(
			EntityFilter::getCodec,
			EntityFilter::type
	);
}
