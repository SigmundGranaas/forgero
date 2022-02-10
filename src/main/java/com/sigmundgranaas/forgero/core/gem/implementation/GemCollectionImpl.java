package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemCollection;

import java.util.List;

public class GemCollectionImpl implements GemCollection {
    private static GemCollectionImpl INSTANCE;
    private static List<Gem> gems;

    public static GemCollection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GemCollectionImpl();
        }
        return INSTANCE;
    }

    @Override
    public List<Gem> getGems() {
        if (gems == null) {
            gems = new FileGemLoader().loadGems();
        }
        return gems;
    }
}
