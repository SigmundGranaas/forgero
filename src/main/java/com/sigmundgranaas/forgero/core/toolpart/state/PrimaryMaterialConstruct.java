package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.constructable.Construct;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;

public record PrimaryMaterialConstruct(PrimaryMaterial material) implements Construct<PrimaryMaterial> {
    @Override
    public PrimaryMaterial getResource() {
        return material;
    }

    @Override
    public String getConstructIdentifier() {
        return material.getStringIdentifier();
    }
}
