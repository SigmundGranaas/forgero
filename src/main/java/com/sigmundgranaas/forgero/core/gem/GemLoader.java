package com.sigmundgranaas.forgero.core.gem;

import com.sigmundgranaas.forgero.core.gem.implementation.BasicGemLoader;

import java.util.List;

public interface GemLoader {
    GemLoader INSTANCE = new BasicGemLoader();

    List<Gem> loadGems();
}
