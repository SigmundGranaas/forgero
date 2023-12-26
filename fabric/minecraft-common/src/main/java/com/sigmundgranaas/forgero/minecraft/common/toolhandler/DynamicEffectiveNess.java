package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockEfficiencyFeature;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;


public interface DynamicEffectiveNess {

	default boolean isEffective(BlockState state, ItemStack stack) {
		return effectiveBlocks(stack).stream().anyMatch(state::isIn);
	}

	default List<TagKey<Block>> effectiveBlocks(ItemStack stack) {
		return StateService.INSTANCE.convert(stack)
				.map(PropertyContainer::stream)
				.map(stream -> stream.features(BlockEfficiencyFeature.KEY))
				.flatMap(stream -> stream.reduce(BlockEfficiencyFeature.REDUCER))
				.map(BlockEfficiencyFeature::tags)
				.orElse(Collections.emptyList());
	}

	boolean isEffectiveOn(BlockState state, ItemStack stack);
}
