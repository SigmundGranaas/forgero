package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;

public interface ToolPartPropertyStrategy {
    int getDurability();

    ForgeroMaterial getMaterial();

    PrimaryMaterial getPrimaryMaterial();

    default SecondaryMaterial getSecondaryMaterial() {
        return new EmptySecondaryMaterial();
    }
}

