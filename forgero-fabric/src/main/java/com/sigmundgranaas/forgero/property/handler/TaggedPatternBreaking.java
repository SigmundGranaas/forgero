package com.sigmundgranaas.forgero.property.handler;

import com.sigmundgranaas.forgero.property.ActivePropertyType;
import com.sigmundgranaas.forgero.property.active.ActiveProperty;
import com.sigmundgranaas.forgero.property.active.BreakingDirection;
import com.sigmundgranaas.forgero.resource.data.PropertyPojo;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.function.Function;
import java.util.function.Predicate;

public class TaggedPatternBreaking extends PatternBreaking {
    public static Predicate<PropertyPojo.Active> predicate = (active) -> active.type == ActivePropertyType.TAGGED_BLOCK_BREAKING_PATTERN && active.tag != null;
    public static Function<PropertyPojo.Active, ActiveProperty> factory = (active) -> new TaggedPatternBreaking(active.pattern, active.direction == null ? BreakingDirection.ANY : active.direction, active.tag);

    private final String tag;

    public TaggedPatternBreaking(String[] pattern, BreakingDirection any, String tag) {
        super(pattern, any);
        this.tag = tag;
    }

    @Override
    public boolean checkBlock(BlockState state) {
        return state.isIn(TagKey.of(RegistryKeys.BLOCK, new Identifier(tag)));
    }
}
