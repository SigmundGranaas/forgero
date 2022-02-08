package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemLoader;

import java.util.List;
import java.util.stream.Collectors;

public class FileGemLoader implements GemLoader {

    @Override
    public List<Gem> loadGems() {
        GemFactory factory = new GemFactory();
        return new JsonFolderLoader("/data/forgero/gems/").loadMaterials(GemPOJO.class).stream().map(factory::createGem).collect(Collectors.toList());
    }
}
