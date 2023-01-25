package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface DynamicMiningLevel {
    int getMiningLevel(ItemStack stack);

    int getMiningLevel();

    default boolean isCorrectMiningLevel(BlockState state, int miningLevel) {
        for (int i = 1; i < 10; i++) {
            TagKey<Block> key = TagKey.of(Registry.BLOCK_KEY, new Identifier(String.format("fabric:needs_tool_level_%s", i)));
            if (state.isIn(key) && miningLevel < i) {
                return false;
            }
        }

        if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) && miningLevel < 3) {
            return false;
        } else if (state.isIn(BlockTags.NEEDS_IRON_TOOL) && miningLevel < 2) {
            return false;
        } else if (state.isIn(BlockTags.NEEDS_STONE_TOOL) && miningLevel < 1) {
            return false;
        } else {
            return true;
        }
    }
}
