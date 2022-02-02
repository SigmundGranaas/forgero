package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.EmptySecondaryMaterial;

public interface ToolPartPropertyStrategy {
    int getDurability();

    ForgeroMaterial getMaterial();

    PrimaryMaterial getPrimaryMaterial();

    default SecondaryMaterial getSecondaryMaterial() {
        return new EmptySecondaryMaterial();
    }
}

