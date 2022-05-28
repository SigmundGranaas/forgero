package com.sigmundgranaas.forgero.core.registry.impl;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.registry.MaterialRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class ConcurrentMaterialRegistry extends ConcurrentResourceRegistry<ForgeroMaterial> implements MaterialRegistry {

    public ConcurrentMaterialRegistry(Map<String, ForgeroMaterial> materials) {
        super(materials);
    }

    @Override
    public @NotNull Optional<ForgeroMaterial> getMaterial(ForgeroToolIdentifier identifier) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<ForgeroMaterial> getMaterial(ForgeroToolPartIdentifier identifier) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<ForgeroMaterial> getMaterial(ForgeroMaterialIdentifier identifier) {
        return Optional.empty();
    }
}
