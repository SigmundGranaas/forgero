package com.sigmundgranaas.forgero.core.gem;

import java.util.Optional;

public abstract class ForgeroGem implements Gem {
    private final int gemLevel;
    private final String identifier;

    public ForgeroGem(int gemLevel, String identifier) {
        if (gemLevel < 1) {
            throw new IllegalArgumentException("Gem level cannot be under 1");
        }
        this.gemLevel = gemLevel;
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getLevel() {
        return gemLevel;
    }

    @Override
    public Optional<Gem> upgradeGem(Gem newGem) {
        if (equals(newGem) && newGem.getIdentifier().equals(this.getIdentifier())) {
            return Optional.of(this.createNewGem(getLevel() + 1, getIdentifier()));
        }
        return Optional.empty();
    }

    public abstract ForgeroGem createNewGem(int level, String Identifier);

    public abstract boolean equals(Gem newGem);

    @Override
    public String getName() {
        return getIdentifier().split("_")[0];
    }
}
