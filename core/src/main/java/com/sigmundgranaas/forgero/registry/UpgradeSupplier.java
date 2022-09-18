package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.state.Upgrade;

import java.util.Optional;

@FunctionalInterface
public interface UpgradeSupplier {
    Optional<Upgrade> get(String id);
}
