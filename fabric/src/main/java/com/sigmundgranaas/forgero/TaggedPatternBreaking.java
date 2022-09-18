package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.property.active.BreakingDirection;
import net.minecraft.block.BlockState;
import net.minecraft.tag.TagKey;
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
        return state.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier(tag)));
    }
}
