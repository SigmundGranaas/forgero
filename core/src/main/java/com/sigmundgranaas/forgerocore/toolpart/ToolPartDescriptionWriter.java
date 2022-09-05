package com.sigmundgranaas.forgerocore.toolpart;

import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgerocore.property.PropertyStream;

public interface ToolPartDescriptionWriter {
    void addSecondaryMaterial(SecondaryMaterial material);

    void addGem(Gem gem);

    void addPrimaryMaterial(PrimaryMaterial material);

    void addToolPartProperties(PropertyStream stream);
}
