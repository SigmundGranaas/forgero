package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleSecondaryMaterialImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MaterialFactoryImpl extends DataResourceFactory<MaterialPojo, ForgeroMaterial> implements MaterialFactory {

    public MaterialFactoryImpl(List<MaterialPojo> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }


    protected MaterialPojo mergePojos(MaterialPojo parent, MaterialPojo material, MaterialPojo basePojo) {
        //Some attributes should always be fetched from the child
        basePojo.resourceType = ResourceType.MATERIAL;
        basePojo.type = replaceAttributesDefault(material.type, parent.type, null);
        basePojo.primary = material.primary;
        basePojo.secondary = material.secondary;
        basePojo.palette = material.palette;


        basePojo.ingredient = replaceAttributesDefault(material.ingredient, parent.ingredient, null);

        return basePojo;
    }

    @Override
    protected MaterialPojo createDefaultPojo() {
        return new MaterialPojo();
    }

    @Override
    public @NotNull Optional<ForgeroMaterial> createResource(MaterialPojo pojo) {
        if (pojo.primary != null) {
            return Optional.of(new SimpleDuoMaterial(pojo));
        } else if (pojo.secondary != null) {
            return Optional.of(new SimpleSecondaryMaterialImpl(pojo));
        } else {
            return Optional.empty();
        }
    }
}
