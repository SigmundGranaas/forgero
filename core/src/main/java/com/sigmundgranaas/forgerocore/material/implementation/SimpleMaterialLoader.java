package com.sigmundgranaas.forgerocore.material.implementation;


import com.sigmundgranaas.forgerocore.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgerocore.data.factory.MaterialFactory;
import com.sigmundgranaas.forgerocore.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgerocore.material.MaterialLoader;
import com.sigmundgranaas.forgerocore.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgerocore.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgerocore.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgerocore.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgerocore.util.JsonPOJOLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record SimpleMaterialLoader(List<String> materials) implements MaterialLoader {
    public static final HashMap<String, ForgeroMaterial> materialMap = new HashMap<>();

    @Override
    public Map<String, ForgeroMaterial> getMaterials() {
        if (materialMap.isEmpty()) {
            List<MaterialPojo> jsonMaterials = materials.stream()
                    .map(material -> JsonPOJOLoader.loadPOJO(String.format("/data/forgero/materials/%s.json", material), MaterialPojo.class))
                    .filter(Optional::isPresent)
                    .map(Optional::get).toList();
            jsonMaterials.forEach(pojo -> {
                List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
                List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
                PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
            });
            var factory = MaterialFactory.createFactory(jsonMaterials, ForgeroDataResource.DEFAULT_DEPENDENCIES_SET);
            return jsonMaterials.stream().map(factory::buildResource).flatMap(Optional::stream).collect(Collectors.toMap(ForgeroMaterial::getResourceName, (material) -> material));
        }
        return materialMap;
    }
}