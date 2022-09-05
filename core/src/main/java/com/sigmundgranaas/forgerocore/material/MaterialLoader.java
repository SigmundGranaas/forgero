package com.sigmundgranaas.forgerocore.material;

import com.sigmundgranaas.forgerocore.exception.NoMaterialsException;
import com.sigmundgranaas.forgerocore.material.implementation.SimpleMaterialLoader;
import com.sigmundgranaas.forgerocore.material.material.ForgeroMaterial;

import java.util.List;
import java.util.Map;

public interface MaterialLoader {
    static MaterialLoader INSTANCE(List<String> materials) {
        return new SimpleMaterialLoader(materials);
    }

    Map<String, ForgeroMaterial> getMaterials() throws NoMaterialsException;
}
