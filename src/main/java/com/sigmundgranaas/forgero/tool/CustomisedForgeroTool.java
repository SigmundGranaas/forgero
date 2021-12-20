package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHead;
import net.minecraft.item.ToolMaterial;
import org.jetbrains.annotations.NotNull;

public class CustomisedForgeroTool extends AbstractForgeroTool {
    protected CustomisedForgeroTool(ToolPartHead head, ToolPartHandle handle) {
        super(head, handle);
    }

    @Override
    public @NotNull ForgeroToolIdentifier getIdentifier() {
        return null;
    }

    @Override
    public @NotNull String getShortToolIdentifierString() {
        return null;
    }

    @Override
    public @NotNull String getToolIdentifierString() {
        return null;
    }


    @Override
    public @NotNull ForgeroToolTypes getToolType() {
        return null;
    }

    @Override
    public int getDurability() {
        return 0;
    }

    @Override
    public int getAttackDamage() {
        return 0;
    }

    @Override
    public float getAttackSpeed() {
        return 0;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 0;
    }

    @Override
    public ToolMaterial getMaterial() {
        return null;
    }
}
