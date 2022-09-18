package com.sigmundgranaas.forgero.registry;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.ForgeroResourceRegistry;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface MaterialRegistry extends ForgeroResourceRegistry<ForgeroMaterial> {

    default ImmutableList<PrimaryMaterial> getPrimaryMaterials() {
        return getSubTypeAsList(PrimaryMaterial.class);
    }

    default ImmutableList<SecondaryMaterial> getSecondaryMaterials() {
        return getSubTypeAsList(SecondaryMaterial.class);
    }

    default Optional<PrimaryMaterial> getPrimaryMaterial(String identifier) {
        return getResource(identifier).map(PrimaryMaterial.class::cast);
    }

    default Optional<SecondaryMaterial> getSecondaryMaterial(String identifier) {
        return getResource(identifier).map(SecondaryMaterial.class::cast);
    }

    @NotNull
    Optional<ForgeroMaterial> getMaterial(ForgeroToolIdentifier identifier);

    @NotNull
    Optional<ForgeroMaterial> getMaterial(ForgeroToolPartIdentifier identifier);

    @NotNull
    Optional<ForgeroMaterial> getMaterial(ForgeroMaterialIdentifier identifier);
}
