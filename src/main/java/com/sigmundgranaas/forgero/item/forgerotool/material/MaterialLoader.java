package com.sigmundgranaas.forgero.item.forgerotool.material;

import com.sigmundgranaas.forgero.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.Material;

import java.util.Map;

public interface MaterialLoader {
    MaterialLoader INSTANCE = MaterialLoaderImpl.getInstance();


    Map<String, Material> getMaterials() throws NoMaterialsException;
}
