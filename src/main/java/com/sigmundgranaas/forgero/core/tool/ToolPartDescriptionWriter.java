package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;

public interface ToolPartDescriptionWriter {
    void addSecondaryMaterial(SecondaryMaterial material);

    void addGem();

    void addPrimaryMaterial(PrimaryMaterial material);

    void addMiningLevel(int miningLevel);

    void addMiningMultiplier(float mulitipler);
}
