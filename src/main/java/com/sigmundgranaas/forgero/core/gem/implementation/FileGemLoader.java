package com.sigmundgranaas.forgero.core.gem.implementation;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemLoader;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgero.core.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.utils.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileGemLoader implements GemLoader {

    private static final List<String> gems = List.of("diamond", "emerald", "ender", "lapis", "redstone");

    @Override
    public List<Gem> loadGems() {
        GemFactory factory = new GemFactory();
        List<GemPOJO> gemPojos = gems.stream()
                .map(gem -> loadMaterial(String.format("/data/forgero/gems/%s.json", gem), GemPOJO.class))
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

    /**
     * @return MaterialPOJO
     * @throws NoMaterialsException - if the method cannot retrieve any materials
     */
    public <T> Optional<T> loadMaterial(String filePath, Class<T> type) {

        InputStream materialsStream = Utils.readJsonResourceAsString(filePath);

        if (materialsStream != null) {
            JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
            return Optional.of(new Gson().fromJson(materialsJson, type));
        } else {
            ForgeroInitializer.LOGGER.error("Unable to load: {}", filePath);
            return Optional.empty();
        }
    }
}
