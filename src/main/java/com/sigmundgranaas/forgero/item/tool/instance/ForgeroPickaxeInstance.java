package com.sigmundgranaas.forgero.item.tool.instance;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.item.ForgeroToolInstance;
import org.jetbrains.annotations.NotNull;

public class ForgeroPickaxeInstance extends ForgeroToolInstance {
    public ForgeroPickaxeInstance(@NotNull ForgeroTool forgeroTool) {
        super(forgeroTool);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.PICKAXE;
    }

    public float getMiningSpeedMultiplier() {
        return 100.0F;
    }

    public float getDamageMultiplier() {
        return 2.0F;
    }
}
