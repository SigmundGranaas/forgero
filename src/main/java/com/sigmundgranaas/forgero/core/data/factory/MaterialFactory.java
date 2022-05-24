package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.pojo.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;

import java.util.List;
import java.util.Set;

public interface MaterialFactory {
    MaterialFactory INSTANCE = MaterialFactoryImpl.getInstance();

    static DataResourceFactory<SimpleMaterialPOJO, ForgeroMaterial> createFactory(List<SimpleMaterialPOJO> pojos, Set<String> availableNameSpaces) {
        return new MaterialFactoryImpl(pojos, availableNameSpaces);
    }
}
