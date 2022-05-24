package com.sigmundgranaas.forgero.core.material.material.factory;

import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MaterialFactory {
    MaterialFactory INSTANCE = MaterialFactoryImpl.getInstance();

    static MaterialFactory createFactory(List<SimpleMaterialPOJO> pojos, Set<String> availableNameSpaces) {
        return new MaterialFactoryImpl(pojos, availableNameSpaces);
    }

    Optional<ForgeroMaterial> createMaterial(SimpleMaterialPOJO material);
}
