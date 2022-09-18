package com.sigmundgranaas.forgero.gem;

import java.util.Collections;

public class EmptyGem extends ForgeroGem {
    public EmptyGem(int gemLevel, String identifier) {
        super(gemLevel, identifier, Collections.emptyList(), Collections.emptyList());
    }

    public static EmptyGem createEmptyGem() {
        return new EmptyGem(1, "empty_gem");
    }

    @Override
    public boolean equals(Gem newGem) {
        return newGem instanceof EmptyGem;
    }

    @Override
    public Gem createGem(int level) {
        return createEmptyGem();
    }

}
