package com.sigmundgranaas.forgero.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Represents a feature that modulates the efficiency of block interactions based on specified criteria.
 * The purpose is to enhance the gameplay by making certain blocks more or less effective to interact with
 * under defined circumstances.
 *
 * <p><h3>JSON Configuration Structure:</h3>
 * The configuration for this feature is represented in JSON format as follows:
 * <pre>
 * {
 *   "type": "minecraft:block_effectiveness",
 *   "tags": [
 *     "minecraft:mineable/pickaxe"
 *   ]
 * }
 * </pre>
 */
@Getter
@Accessors(fluent = true)
public class BlockEfficiencyFeature extends BasePredicateFeature {
	public static final String BLOCK_EFFICIENCY_TYPE = "minecraft:block_effectiveness";
	public static final ClassKey<BlockEfficiencyFeature> KEY = new ClassKey<>(BLOCK_EFFICIENCY_TYPE, BlockEfficiencyFeature.class);
	public static final FeatureBuilder<BlockEfficiencyFeature> BUILDER = FeatureBuilder.of(BLOCK_EFFICIENCY_TYPE, BlockEfficiencyFeature::buildFromBase);

	public static final BinaryOperator<BlockEfficiencyFeature> REDUCER = (feature1, feature2) -> {
		List<TagKey<Block>> combinedTags = new ArrayList<>(feature1.tags());
		combinedTags.addAll(feature2.tags());
		combinedTags = combinedTags.stream().distinct().collect(Collectors.toList());
		return new BlockEfficiencyFeature(feature1.data(), combinedTags);
	};

	private final List<TagKey<Block>> tags;

	public BlockEfficiencyFeature(BasePredicateData data, List<TagKey<Block>> tags) {
		super(data);
		this.tags = tags;
		if (!data.type().equals(BLOCK_EFFICIENCY_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + BLOCK_EFFICIENCY_TYPE);
		}
	}

	private static BlockEfficiencyFeature buildFromBase(BasePredicateData data, JsonElement element) {
		List<Identifier> extractedTags = new ArrayList<>();
		if (element.isJsonObject() && element.getAsJsonObject().has("tags")) {
			element.getAsJsonObject().getAsJsonArray("tags").forEach(tagElement -> {
				extractedTags.add(new Identifier(tagElement.getAsString()));
			});
		}
		return new BlockEfficiencyFeature(data, extractedTags.stream()
				.map(id -> TagKey.of(RegistryKeys.BLOCK, id))
				.collect(Collectors.toList()));
	}
}
