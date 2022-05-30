package com.sigmundgranaas.forgero.toolhandler;

import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.TargetTypes;
import net.minecraft.block.BlockState;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.Set;

public record BlockBreakingEfficiencyTarget(BlockState state) implements Target {
    @Override
    public Set<TargetTypes> getTypes() {
        return Set.of(TargetTypes.BLOCK);
    }

    @Override
    public Set<String> getTags() {
        return Collections.emptySet();
    }

    @Override
    public boolean isApplicable(Set<String> tag, TargetTypes type) {
        if (type == TargetTypes.BLOCK) {
            for (String stringTag : tag) {
                try {
                    return state.isIn(ServerTagManagerHolder.getTagManager().getTag(Registry.BLOCK_KEY, new Identifier(stringTag), (t) -> new Exception()));
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }
}
