package com.sigmundgranaas.forgero.core.material.implementation;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record SimpleMaterialLoader(List<String> materials) implements MaterialLoader {
    public static final HashMap<String, ForgeroMaterial> materialMap = new HashMap<>();
    public static final Logger LOGGER = ForgeroInitializer.LOGGER;

    @Override
    public Map<String, ForgeroMaterial> getMaterials() {
        if (materialMap.isEmpty()) {
            List<SimpleMaterialPOJO> jsonMaterials = materials.stream()
                    .map(material -> JsonPOJOLoader.loadPOJO(String.format("/data/forgero/materials/simple/%s.json", material), SimpleMaterialPOJO.class))
                    .filter(Optional::isPresent)
                    .map(Optional::get).toList();
            jsonMaterials.forEach(pojo -> {
                List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
                List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
                PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
            });
            //jsonMaterials.stream().sorted(Comparator.comparingInt(material -> material.rarity)).forEach(material -> materialMap.put(material.name.toLowerCase(Locale.ROOT), MaterialFactory.INSTANCE.createMaterial(material)));
        }
        return materialMap;
    }


}




