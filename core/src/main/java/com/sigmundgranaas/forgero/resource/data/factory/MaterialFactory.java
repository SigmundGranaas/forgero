package com.sigmundgranaas.forgero.resource.data.factory;

import com.sigmundgranaas.forgero.resource.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;

import java.util.List;
import java.util.Set;

public interface MaterialFactory {
    static DataResourceFactory<MaterialPojo, ForgeroMaterial> createFactory(List<MaterialPojo> pojos, Set<String> availableNameSpaces) {
        return new MaterialFactoryImpl(pojos, availableNameSpaces);
    }
}
