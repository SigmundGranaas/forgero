package com.sigmundgranaas.forgero.item.forgerotool.material.material.factory;

import com.sigmundgranaas.forgero.item.forgerotool.material.material.Material;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.MaterialPOJO;

public interface MaterialFactory {
    MaterialFactory INSTANCE = MaterialFactoryImpl.getInstance();

    Material createMaterial(MaterialPOJO material);
}
