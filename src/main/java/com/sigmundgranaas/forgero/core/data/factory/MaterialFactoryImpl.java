package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.pojo.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleSecondaryMaterialImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MaterialFactoryImpl extends DataResourceFactory<SimpleMaterialPOJO, ForgeroMaterial> implements MaterialFactory {
    private static MaterialFactoryImpl INSTANCE;

    public MaterialFactoryImpl() {
    }

    public MaterialFactoryImpl(List<SimpleMaterialPOJO> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }


    public static MaterialFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MaterialFactoryImpl();
        }
        return INSTANCE;
    }

    protected SimpleMaterialPOJO mergePojos(SimpleMaterialPOJO parent, SimpleMaterialPOJO material, SimpleMaterialPOJO basePojo) {
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
    protected SimpleMaterialPOJO createDefaultPojo() {
        return new SimpleMaterialPOJO();
    }

    @Override
    protected Optional<ForgeroMaterial> createResource(SimpleMaterialPOJO pojo) {
        if (pojo.primary != null) {
            return Optional.of(new SimpleDuoMaterial(pojo));
        } else {
            return Optional.of(new SimpleSecondaryMaterialImpl(pojo));
        }
    }
}
