package com.sigmundgranaas.forgero.core.material;

import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.material.implementation.SimpleMaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;

import java.util.List;
import java.util.Map;

public interface MaterialLoader {
    static MaterialLoader INSTANCE(List<String> materials) {
        return new SimpleMaterialLoader(materials);
    }

    Map<String, ForgeroMaterial> getMaterials() throws NoMaterialsException;
}
