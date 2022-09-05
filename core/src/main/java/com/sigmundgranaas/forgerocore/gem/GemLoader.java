package com.sigmundgranaas.forgerocore.gem;

import com.sigmundgranaas.forgerocore.gem.implementation.BasicGemLoader;

import java.util.List;

public interface GemLoader {
    GemLoader INSTANCE = new BasicGemLoader();

    List<Gem> loadGems();
}
