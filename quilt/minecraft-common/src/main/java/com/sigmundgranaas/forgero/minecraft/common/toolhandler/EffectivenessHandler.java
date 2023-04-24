package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;

import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_TYPE_KEY;

public class EffectivenessHandler {
	public static String EFFECTIVENESS_KEY = "EFFECTIVE_BLOCKS";

	public static List<TagKey<Block>> of(PropertyContainer container) {
		var key = PropertyTargetCacheKey.of(container, EFFECTIVENESS_KEY);
		boolean has = ContainsFeatureCache.check(key);
		boolean disabled = ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, BROKEN_TYPE_KEY));
		if (has && !disabled) {
			return container.stream()
					.features()
					.filter(feature -> feature.type().equals(EFFECTIVENESS_KEY))
					.map(PropertyData::getTags)
					.flatMap(List::stream)
					.map(tag -> TagKey.of(Registry.BLOCK_KEY, new Identifier(tag)))
					.toList();
		}
		return Collections.emptyList();
	}

}
