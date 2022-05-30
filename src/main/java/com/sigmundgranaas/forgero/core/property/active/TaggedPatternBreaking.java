package com.sigmundgranaas.forgero.core.property.active;

import net.minecraft.block.BlockState;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TaggedPatternBreaking extends PatternBreaking {
    private final String tag;

    public TaggedPatternBreaking(String[] pattern, BreakingDirection any, String tag) {
        super(pattern, any);
        this.tag = tag;
    }

    @Override
    public boolean checkBlock(BlockState state) {
        try {
            return state.isIn(ServerTagManagerHolder.getTagManager().getTag(Registry.BLOCK_KEY, new Identifier(tag), (tag) -> new Exception()));
        } catch (Exception e) {
            return false;
        }
    }
}
