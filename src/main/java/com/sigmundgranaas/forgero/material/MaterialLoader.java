package com.sigmundgranaas.forgero.material;

import com.sigmundgranaas.forgero.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;

import java.util.Map;

public interface MaterialLoader {
    MaterialLoader INSTANCE = MaterialLoaderImpl.getInstance();


    Map<String, ForgeroMaterial> getMaterials() throws NoMaterialsException;
}
