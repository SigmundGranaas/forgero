package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.v1.pojo.MaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleSecondaryMaterialImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MaterialFactoryImpl extends DataResourceFactory<MaterialPOJO, ForgeroMaterial> implements MaterialFactory {

    public MaterialFactoryImpl(List<MaterialPOJO> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }


    protected MaterialPOJO mergePojos(MaterialPOJO parent, MaterialPOJO material, MaterialPOJO basePojo) {
        //Some attributes should always be fetched from the child
        basePojo.resourceType = ResourceType.MATERIAL;
        basePojo.type = replaceAttributesDefault(material.type, parent.type, null);
        basePojo.primary = material.primary;
        basePojo.secondary = material.secondary;
        basePojo.palette = material.palette;

        //TODO create a proper way of handling ingredients
        basePojo.ingredient = replaceAttributesDefault(material.ingredient, parent.ingredient, null);

        return basePojo;
    }

    @Override
    protected MaterialPOJO createDefaultPojo() {
        return new MaterialPOJO();
    }

    @Override
    protected Optional<ForgeroMaterial> createResource(MaterialPOJO pojo) {
        if (pojo.primary != null) {
            return Optional.of(new SimpleDuoMaterial(pojo));
        } else {
            return Optional.of(new SimpleSecondaryMaterialImpl(pojo));
        }
    }
}
