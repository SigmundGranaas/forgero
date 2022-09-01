package com.sigmundgranaas.forgero.item.items.tool;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.MatchContext;
import com.sigmundgranaas.forgero.core.util.Matchable;
import com.sigmundgranaas.forgero.core.util.Type;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;

public class DynamicSwordItem extends SwordItem implements State {
    private final State DEFAULT;

    public DynamicSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, State defaultState) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public String name() {
        return DEFAULT.name();
    }

    @Override
    public Type type() {
        return DEFAULT.type();
    }

    @Override
    public boolean test(Matchable match) {
        return DEFAULT.test(match);
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        return DEFAULT.test(match, context);
    }
}

