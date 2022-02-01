package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.head.ToolPartHead;
import net.minecraft.item.ToolMaterial;
import org.jetbrains.annotations.NotNull;

public interface ForgeroTool {
    @NotNull
    ToolPartHead getToolHead();

    @NotNull
    PrimaryMaterial getPrimaryMaterial();

    @NotNull
    ToolPartHandle getToolHandle();

    @NotNull
    ForgeroToolIdentifier getIdentifier();

    @NotNull
    String getShortToolIdentifierString();

    @NotNull
    String getToolIdentifierString();

    @NotNull
    ForgeroToolTypes getToolType();

    int getDurability();

    int getAttackDamage();

    float getAttackSpeed();

    float getMiningSpeedMultiplier();

    int getMiningLevel();

    ToolMaterial getMaterial();

    void createToolDescription(ToolDescriptionWriter writer);
}
