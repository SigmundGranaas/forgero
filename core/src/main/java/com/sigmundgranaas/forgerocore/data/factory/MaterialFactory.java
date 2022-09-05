package com.sigmundgranaas.forgerocore.data.factory;

import com.sigmundgranaas.forgerocore.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgerocore.material.material.ForgeroMaterial;

import java.util.List;
import java.util.Set;

public interface MaterialFactory {
    static DataResourceFactory<MaterialPojo, ForgeroMaterial> createFactory(List<MaterialPojo> pojos, Set<String> availableNameSpaces) {
        return new MaterialFactoryImpl(pojos, availableNameSpaces);
    }
}
