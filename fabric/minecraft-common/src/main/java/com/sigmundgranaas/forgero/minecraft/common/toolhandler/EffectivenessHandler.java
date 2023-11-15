package com.sigmundgranaas.forgero.minecraft.common.toolhandler;


import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureContainerKey;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockEffectivenessFeature;
import net.minecraft.block.Block;
import net.minecraft.registry.tag.TagKey;


import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_KEY;
import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.streamFeature;

public class EffectivenessHandler {
	public static List<TagKey<Block>> of(PropertyContainer container) {
		boolean disabled = FeatureCache.check(FeatureContainerKey.of(container, BROKEN_KEY));
		if (!disabled) {
			return streamFeature(container, MatchContext.of(), BlockEffectivenessFeature.KEY)
					.reduce(BlockEffectivenessFeature.REDUCER)
					.map(BlockEffectivenessFeature::keys)
					.orElseGet(Collections::emptyList);
		}
		return Collections.emptyList();
	}
}
