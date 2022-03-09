package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemLoader;

import java.util.Collections;
import java.util.List;

public class BasicGemLoader implements GemLoader {
    @Override
    public List<Gem> loadGems() {
        return Collections.emptyList();
    }
}
