package com.sigmundgranaas.forgero.core.toolpart.strategy.realistic;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticPrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticSecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HandleMaterialStrategy;

public class RealisticHandleStrategy extends RealisticDuoMaterialToolPartStrategy implements HandleMaterialStrategy {
    public RealisticHandleStrategy(RealisticPrimaryMaterial material, RealisticSecondaryMaterial secondaryMaterial) {
        super(material, secondaryMaterial);
    }
}
