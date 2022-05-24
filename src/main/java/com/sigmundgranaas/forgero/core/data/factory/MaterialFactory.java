package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.pojo.MaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;

import java.util.List;
import java.util.Set;

public interface MaterialFactory {
    static DataResourceFactory<MaterialPOJO, ForgeroMaterial> createFactory(List<MaterialPOJO> pojos, Set<String> availableNameSpaces) {
        return new MaterialFactoryImpl(pojos, availableNameSpaces);
    }
}
