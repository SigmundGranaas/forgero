package com.sigmundgranaas.forgero.core.material.material.realistic;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;

public interface RealisticPrimaryMaterial extends PrimaryMaterial {
    int getStiffness();

    int getSharpness();
}
