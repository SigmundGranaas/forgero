package com.sigmundgranaas.forgero.core.gem;

import com.sigmundgranaas.forgero.core.gem.implementation.GemCollectionImpl;

import java.util.List;

public interface GemCollection {
    GemCollection INSTANCE = GemCollectionImpl.getInstance();

    List<Gem> getGems();
}
