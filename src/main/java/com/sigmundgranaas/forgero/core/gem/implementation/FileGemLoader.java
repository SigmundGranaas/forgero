package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemLoader;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgero.core.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record FileGemLoader(List<String> gems) implements GemLoader {

    @Override
    public List<Gem> loadGems() {
        GemFactory factory = new GemFactory();
        List<GemPOJO> gemPojos = gems.stream()
                .map(gem -> JsonPOJOLoader.loadPOJO(String.format("/data/forgero/gems/%s.json", gem), GemPOJO.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        gemPojos.forEach(pojo -> {
            List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
        });
        return gemPojos.stream().map(factory::createGem).collect(Collectors.toList());
    }
}
