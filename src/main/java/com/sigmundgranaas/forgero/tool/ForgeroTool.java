package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.registry.ItemRegister;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

public interface ForgeroTool {
    default void registerToolItem(ItemRegister register) {
        register.registerTool(this);
    }

    @NotNull
    ForgeroToolPart getToolHead();

    @NotNull
    PrimaryMaterial getPrimaryMaterial();

    @NotNull
    ForgeroToolPart getToolHandle();

    @NotNull
    ForgeroToolIdentifier getIdentifier();

    @NotNull
    String getShortToolIdentifierString();

    @NotNull
    String getToolIdentifierString();

    @NotNull
    ForgeroToolTypes getToolType();
}
