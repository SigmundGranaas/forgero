package com.sigmundgranaas.forgero.core.material.implementation;

import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.material.MaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;

import java.util.Map;

public class SimpleMaterialLoader implements MaterialLoader {
    @Override
    public Map<String, ForgeroMaterial> getMaterials() throws NoMaterialsException {
        return null;
    }
}
