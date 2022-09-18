package com.sigmundgranaas.forgero.material;

import com.sigmundgranaas.forgero.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.material.implementation.SimpleMaterialLoader;
import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;

import java.util.List;
import java.util.Map;

public interface MaterialLoader {
    static MaterialLoader INSTANCE(List<String> materials) {
        return new SimpleMaterialLoader(materials);
    }

    Map<String, ForgeroMaterial> getMaterials() throws NoMaterialsException;
}
