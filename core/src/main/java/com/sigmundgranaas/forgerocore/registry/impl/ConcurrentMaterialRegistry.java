package com.sigmundgranaas.forgerocore.registry.impl;

import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgerocore.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgerocore.registry.MaterialRegistry;
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
