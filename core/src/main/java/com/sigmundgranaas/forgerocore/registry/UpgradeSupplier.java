package com.sigmundgranaas.forgerocore.registry;

import com.sigmundgranaas.forgerocore.state.Upgrade;

import java.util.Optional;

@FunctionalInterface
public interface UpgradeSupplier {
    Optional<Upgrade> get(String id);
}
