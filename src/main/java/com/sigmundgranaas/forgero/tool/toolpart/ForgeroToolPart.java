package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;

public interface ForgeroToolPart {
    PrimaryMaterial getPrimaryMaterial();

    SecondaryMaterial getSecondaryMaterial();

    int getWeight();

    float getWeightScale();

    int getDurability();

    int getDurabilityScale();

    String getToolTypeName();

    String getToolPartName();

    ForgeroToolPartTypes getToolpartType();
}
