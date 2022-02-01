package com.sigmundgranaas.forgero.core.material.material.factory;

import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.DuoMaterialImpl;
import com.sigmundgranaas.forgero.core.material.material.realistic.PrimaryMaterialImpl;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.realistic.SecondaryMaterialImpl;

public class MaterialFactoryImpl implements MaterialFactory {
    private static MaterialFactoryImpl INSTANCE;

    public static MaterialFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MaterialFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public ForgeroMaterial createMaterial(RealisticMaterialPOJO material) {
        if (material.primary != null && material.secondary != null) {
            return new DuoMaterialImpl(material);
        } else if (material.primary != null) {
            return new PrimaryMaterialImpl(material);
        } else {
            return new SecondaryMaterialImpl(material);
        }
    }
}
