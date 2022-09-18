package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.registry.MaterialRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class ConcurrentMaterialRegistry extends ConcurrentResourceRegistry<ForgeroMaterial> implements MaterialRegistry {
    public ConcurrentMaterialRegistry(Map<String, ForgeroMaterial> materials) {
        super(materials);
    }

    @Override
    public @NotNull Optional<ForgeroMaterial> getMaterial(ForgeroToolIdentifier identifier) {
        return getResource(identifier.getMaterial().getName());
    }

    @Override
    public @NotNull Optional<ForgeroMaterial> getMaterial(ForgeroToolPartIdentifier identifier) {
        return getResource(identifier.getMaterial().getName());
    }

    @Override
    public @NotNull Optional<ForgeroMaterial> getMaterial(ForgeroMaterialIdentifier identifier) {
        return getResource(identifier.getName());
    }
}
