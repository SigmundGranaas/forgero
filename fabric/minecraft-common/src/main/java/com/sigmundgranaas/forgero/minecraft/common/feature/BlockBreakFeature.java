package com.sigmundgranaas.forgero.minecraft.common.feature;

import java.util.Set;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.BlockBreakHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.BlockHardnessCalculator;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.BlockSelector;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class BlockBreakFeature extends BasePredicateFeature implements BlockBreakHandler {
	public static final String BLOCK_BREAK_TYPE = "minecraft:block_breaking";
	public static final ClassKey<BlockBreakFeature> KEY = new ClassKey<>(BLOCK_BREAK_TYPE, BlockBreakFeature.class);
	public static final String SELECTOR = "selector";
	public static final String HARDNESS = "hardness";
	public static final FeatureBuilder<BlockBreakFeature> BUILDER = FeatureBuilder.of(BLOCK_BREAK_TYPE, BlockBreakFeature::buildFromBase);
	private final BlockSelector selector;
	private final BlockHardnessCalculator hardnessCalculator;

	public BlockBreakFeature(BasePredicateData data, BlockSelector selector, BlockHardnessCalculator calculator) {
		super(data);
		this.selector = selector;
		this.hardnessCalculator = calculator;
		if (!data.type().equals(BLOCK_BREAK_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + BLOCK_BREAK_TYPE);
		}
	}

	private static BlockBreakFeature buildFromBase(BasePredicateData data, JsonElement element) {
		BlockSelector selector = BlockSelector.DEFAULT;
		BlockHardnessCalculator blockHardnessCalculator = BlockHardnessCalculator.DEFAULT;
		if (element.isJsonObject() && element.getAsJsonObject().has(SELECTOR)) {
			var object = element.getAsJsonObject();
			var handlerOpt = HandlerBuilder.DEFAULT.build(BlockSelector.KEY, object.get(SELECTOR));
			if (handlerOpt.isPresent()) {
				selector = handlerOpt.get();
			}
		}

		if (element.isJsonObject() && element.getAsJsonObject().has(HARDNESS)) {
			var object = element.getAsJsonObject();
			var handlerOpt = HandlerBuilder.DEFAULT.build(BlockHardnessCalculator.KEY, object.get(HARDNESS));
			if (handlerOpt.isPresent()) {
				blockHardnessCalculator = handlerOpt.get();
			}
		}

		return new BlockBreakFeature(data, selector, blockHardnessCalculator);
	}

	@Override
	public Set<BlockPos> selectBlocks(Entity source, BlockPos target) {
		return selector.select(target);
	}

	@Override
	public float calculateBlockHardness(Entity source, BlockPos target, Set<BlockPos> selectedBlocks) {
		return hardnessCalculator.calculateBlockHardness(source, target, selectedBlocks);
	}

	@Override
	public void onUsed(Entity source, BlockPos target, Set<BlockPos> selectedBlocks) {

	}
}
