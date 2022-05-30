package com.sigmundgranaas.forgero.core.property.active;

import net.minecraft.block.BlockState;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.util.Identifier;

public class TaggedPatternBreaking extends PatternBreaking {
    private final String tag;

    public TaggedPatternBreaking(String[] pattern, BreakingDirection any, String tag) {
        super(pattern, any);
        this.tag = tag;
    }

    @Override
    public boolean checkBlock(BlockState state) {
        try {
            return state.isIn(ServerTagManagerHolder.getTagManager().getBlocks().getTag(new Identifier(tag)));
        } catch (Exception e) {
            return false;
        }
    }
}
