package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.property.PropertyStream;

public interface ToolPartDescriptionWriter {
    void addSecondaryMaterial(SecondaryMaterial material);

    void addGem(Gem gem);

    void addPrimaryMaterial(PrimaryMaterial material);

    void addToolPartProperties(PropertyStream stream);
}
