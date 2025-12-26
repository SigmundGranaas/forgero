package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.filter.TypedFilter;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;
import net.minecraft.entity.Entity;

/**
 * Filters entities based on various criteria.
 * <p>
 * Filters are applied after selection to narrow down the final list of targets.
 * The context is the source entity (e.g., the attacker), and the target is the
 * entity being tested (e.g., a potential victim).
 * <p>
 * Filters are composable and reusable across different selectors.
 *
 * @see com.sigmundgranaas.forgero.common.filter.Filter
 */
public interface EntityFilter extends TypedFilter<Entity, Entity> {
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
