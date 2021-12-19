package com.sigmundgranaas.forgero.material.material.factory;

import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.material.material.MaterialPOJO;

public interface MaterialFactory {
    MaterialFactory INSTANCE = MaterialFactoryImpl.getInstance();

    ForgeroMaterial createMaterial(MaterialPOJO material);
}
