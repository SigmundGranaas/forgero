package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemCollection;

import java.util.List;

public record GemCollectionImpl(List<Gem> gems) implements GemCollection {
    @Override
    public List<Gem> getGems() {
        return gems;
    }
}
