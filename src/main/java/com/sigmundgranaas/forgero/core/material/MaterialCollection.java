package com.sigmundgranaas.forgero.core.material;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.implementation.MaterialCollectionImpl;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface MaterialCollection {
    static MaterialCollection createCollection(Map<String, ForgeroMaterial> materials) {
        return new MaterialCollectionImpl(materials);
    }

    @NotNull
    Map<String, ForgeroMaterial> getMaterialsAsMap();

    @NotNull
    List<ForgeroMaterial> getMaterialsAsList();

    @NotNull
    List<PrimaryMaterial> getPrimaryMaterialsAsList();

    @NotNull
    List<SecondaryMaterial> getSecondaryMaterialsAsList();

    @NotNull
    ForgeroMaterial getMaterial(ForgeroToolIdentifier identifier);

    @NotNull
    ForgeroMaterial getMaterial(ForgeroToolPartIdentifier identifier);

    @NotNull
    ForgeroMaterial getMaterial(ForgeroMaterialIdentifier identifier);
}
