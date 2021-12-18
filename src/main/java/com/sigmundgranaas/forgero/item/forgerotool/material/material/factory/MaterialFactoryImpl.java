package com.sigmundgranaas.forgero.item.forgerotool.material.material.factory;

import com.sigmundgranaas.forgero.item.forgerotool.material.material.*;

public class MaterialFactoryImpl implements MaterialFactory {
    private static MaterialFactoryImpl INSTANCE;

    public static MaterialFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MaterialFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public Material createMaterial(MaterialPOJO material) {
        if (material.primary != null && material.secondary != null) {
            return new DuoMaterialImpl(material);
        } else if (material.primary != null) {
            return new PrimaryMaterialImpl(material);
        } else {
            return new SecondaryMaterialImpl(material);
        }
    }
}
