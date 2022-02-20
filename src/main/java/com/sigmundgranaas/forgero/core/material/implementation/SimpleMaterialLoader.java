package com.sigmundgranaas.forgero.core.material.implementation;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.factory.MaterialFactory;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public record SimpleMaterialLoader(String materialPath) implements MaterialLoader {
    public static final HashMap<String, ForgeroMaterial> materialMap = new HashMap<>();
    public static final Logger LOGGER = ForgeroInitializer.LOGGER;
    private static final List<String> materials = List.of("oak", "spruce", "acacia", "birch", "stone", "iron", "gold", "diamond", "netherite");

    @Override
    public Map<String, ForgeroMaterial> getMaterials() {
        if (materialMap.isEmpty()) {
            List<SimpleMaterialPOJO> jsonMaterials = materials.stream()
                    .map(material -> loadMaterial(String.format("/data/forgero/materials/simple/%s.json", material), SimpleMaterialPOJO.class))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            jsonMaterials.forEach(pojo -> {
                List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
                List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
                PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
            });
            jsonMaterials.forEach(material -> materialMap.put(material.name.toLowerCase(Locale.ROOT), MaterialFactory.INSTANCE.createMaterial(material)));
        }
        return materialMap;
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




