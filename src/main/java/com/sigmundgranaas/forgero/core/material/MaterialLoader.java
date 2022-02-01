package com.sigmundgranaas.forgero.core.material;

import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.material.implementation.RealisticMaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;

import java.util.Map;

public interface MaterialLoader {
    MaterialLoader INSTANCE = RealisticMaterialLoader.getInstance();


    Map<String, ForgeroMaterial> getMaterials() throws NoMaterialsException;
}
