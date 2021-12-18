package com.sigmundgranaas.forgero.item.forgerotool.material;

import com.sigmundgranaas.forgero.item.forgerotool.identifier.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.identifier.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.Material;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.SecondaryMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface MaterialCollection {
    MaterialCollection INSTANCE = MaterialCollectionImpl.getInstance();

    @NotNull Map<String, Material> getMaterialsAsMap();

    @NotNull List<Material> getMaterialsAsList();

    @NotNull List<PrimaryMaterial> getPrimaryMaterialsAsList();

    @NotNull List<SecondaryMaterial> getSecondaryMaterialsAsList();

    @NotNull Material getMaterial(ForgeroToolIdentifier identifier);

    @NotNull Material getMaterial(ForgeroToolPartIdentifier identifier);

    @NotNull Material getMaterial(ForgeroMaterialIdentifier identifier);


}
