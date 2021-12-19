package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;

public interface ForgeroToolPart {
    PrimaryMaterial getPrimaryMaterial();

    SecondaryMaterial getSecondaryMaterial();

    float getHeadDamage();

    String getToolTypeName();

    String getToolPartName();
}
