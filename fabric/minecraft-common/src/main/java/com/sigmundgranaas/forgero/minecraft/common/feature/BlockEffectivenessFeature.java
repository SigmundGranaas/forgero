package com.sigmundgranaas.forgero.minecraft.common.feature;

import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Getter
@Accessors(fluent = true)
public class BlockEffectivenessFeature extends BasePredicateFeature {
	public static final String BLOCK_EFFECTIVENESS_TYPE = "minecraft:block_effectiveness";
	public static final ClassKey<BlockEffectivenessFeature> KEY = new ClassKey<>(BLOCK_EFFECTIVENESS_TYPE, BlockEffectivenessFeature.class);
	public static final FeatureBuilder<BlockEffectivenessFeature> BUILDER = FeatureBuilder.of(BLOCK_EFFECTIVENESS_TYPE, BlockEffectivenessFeature::buildFromBase);
	public static BinaryOperator<BlockEffectivenessFeature> REDUCER = (e1, e2) -> new BlockEffectivenessFeature(e1.data(), Stream.of(e1.tags, e2.tags).flatMap(List::stream).collect(Collectors.toList()));
	private final List<Identifier> tags;

	public BlockEffectivenessFeature(BasePredicateData data, List<Identifier> tags) {
		super(data);
		this.tags = tags;
	}

	private static BlockEffectivenessFeature buildFromBase(BasePredicateData data, JsonElement element) {
		List<Identifier> tags = Collections.emptyList();
		if (element.isJsonObject()) {
			if (element.getAsJsonObject().has("tags")) {
				JsonElement tag = element.getAsJsonObject().get("tags");
				if (tag.isJsonArray()) {
					List<String> parsed = new Gson().fromJson(tag, new TypeToken<List<String>>() {
					}.getType());
					tags = parsed.stream().map(Identifier::new).toList();
				} else if (tag.isJsonPrimitive() && tag.getAsJsonPrimitive().isString()) {
					tags = List.of(new Identifier(tag.getAsString()));
				}
			}
		}
		return new BlockEffectivenessFeature(data, tags);
	}

	public List<TagKey<Block>> keys() {
		return tags.stream().map(tag -> TagKey.of(Registry.BLOCK_KEY, tag)).collect(Collectors.toList());
	}
}
