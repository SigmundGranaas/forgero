package com.sigmundgranaas.forgero.gem.implementation;

import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.gem.GemLoader;

import java.util.Collections;
import java.util.List;

public class BasicGemLoader implements GemLoader {
    @Override
    public List<Gem> loadGems() {
        return Collections.emptyList();
    }
}
