package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemLoader;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgero.core.texture.palette.PaletteResourceRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class FileGemLoader implements GemLoader {

    @Override
    public List<Gem> loadGems() {
        GemFactory factory = new GemFactory();
        List<GemPOJO> gemPojos = new JsonFolderLoader("/data/forgero/gems/").loadMaterials(GemPOJO.class);
        gemPojos.forEach(pojo -> {
            List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
        });
        return gemPojos.stream().map(factory::createGem).collect(Collectors.toList());
    }
}
