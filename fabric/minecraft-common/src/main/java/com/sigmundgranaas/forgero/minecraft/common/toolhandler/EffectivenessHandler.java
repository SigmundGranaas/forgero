package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_TYPE_KEY;
import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.streamFeature;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockEffectivenessFeature;

import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;

public class EffectivenessHandler {
	public static List<TagKey<Block>> of(PropertyContainer container) {
		boolean disabled = ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, BROKEN_TYPE_KEY));
		if (!disabled) {
			return streamFeature(container, MatchContext.of(), BlockEffectivenessFeature.KEY)
					.reduce(BlockEffectivenessFeature.REDUCER)
					.map(BlockEffectivenessFeature::keys)
					.orElseGet(Collections::emptyList);
		}
		return Collections.emptyList();
	}
}
