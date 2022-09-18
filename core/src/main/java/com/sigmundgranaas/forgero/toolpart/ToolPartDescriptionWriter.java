package com.sigmundgranaas.forgero.toolpart;

import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.property.PropertyStream;

public interface ToolPartDescriptionWriter {
    void addSecondaryMaterial(SecondaryMaterial material);

    void addGem(Gem gem);

    void addPrimaryMaterial(PrimaryMaterial material);

    void addToolPartProperties(PropertyStream stream);
}
