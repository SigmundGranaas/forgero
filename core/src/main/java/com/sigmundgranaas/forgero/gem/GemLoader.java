package com.sigmundgranaas.forgero.gem;

import com.sigmundgranaas.forgero.gem.implementation.BasicGemLoader;

import java.util.List;

public interface GemLoader {
    GemLoader INSTANCE = new BasicGemLoader();

    List<Gem> loadGems();
}
