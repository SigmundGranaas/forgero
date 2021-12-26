package com.sigmundgranaas.forgero.core.material;

import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.utils.exception.NoMaterialsException;

import java.util.Map;

public interface MaterialLoader {
    MaterialLoader INSTANCE = MaterialLoaderImpl.getInstance();


    Map<String, ForgeroMaterial> getMaterials() throws NoMaterialsException;
}
