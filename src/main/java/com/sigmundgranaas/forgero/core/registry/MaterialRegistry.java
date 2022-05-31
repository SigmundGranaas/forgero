package com.sigmundgranaas.forgero.core.registry;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.ForgeroResourceRegistry;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
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
