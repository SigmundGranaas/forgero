package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;

import java.util.Collections;
import java.util.List;

public interface DynamicEffectiveNess {

    default boolean isEffective(BlockState state, ItemStack stack) {
        return effectiveBlocks(stack).stream().anyMatch(state::isIn);
    }

    default List<TagKey<Block>> effectiveBlocks(ItemStack stack) {
        return StateConverter.of(stack).map(EffectivenessHandler::of).orElse(Collections.emptyList());
    }

    boolean isEffectiveOn(BlockState state, ItemStack stack);
}
