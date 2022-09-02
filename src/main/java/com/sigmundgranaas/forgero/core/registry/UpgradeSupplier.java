package com.sigmundgranaas.forgero.core.registry;

import com.sigmundgranaas.forgero.core.state.Upgrade;

import java.util.Optional;

@FunctionalInterface
public interface UpgradeSupplier {
    Optional<Upgrade> get(String id);
}
