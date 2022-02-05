package com.sigmundgranaas.forgero.core.gem;

import java.util.Optional;

public interface Gem {
    String getIdentifier();

    GemTypes getType();

    int getLevel();

    Optional<Gem> upgradeGem(Gem newGem);
}
