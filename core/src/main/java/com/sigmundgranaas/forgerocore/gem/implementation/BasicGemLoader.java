package com.sigmundgranaas.forgerocore.gem.implementation;

import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.gem.GemLoader;

import java.util.Collections;
import java.util.List;

public class BasicGemLoader implements GemLoader {
    @Override
    public List<Gem> loadGems() {
        return Collections.emptyList();
    }
}
