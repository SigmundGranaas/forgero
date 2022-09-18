package com.sigmundgranaas.forgero.gem.implementation;

import com.sigmundgranaas.forgero.resource.data.factory.GemFactory;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.GemPojo;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.gem.GemLoader;
import com.sigmundgranaas.forgero.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgero.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.util.JsonPOJOLoader;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record FileGemLoader(List<String> gems) implements GemLoader {

    @Override
    public List<Gem> loadGems() {
        List<GemPojo> gemPojos = gems.stream()
                .map(gem -> JsonPOJOLoader.loadPOJO(String.format("/data/forgero/gems/%s.json", gem), GemPojo.class))
                .filter(Optional::isPresent)
                .map(Optional::get).toList();
        gemPojos.forEach(pojo -> {
            List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
        });
        GemFactory factory = new GemFactory(gemPojos, Set.of("minecraft", "forgero"));
        return gemPojos.stream().map(factory::buildResource).flatMap(Optional::stream).collect(Collectors.toList());
    }
}
