package com.sigmundgranaas.forgero.core.tool.toolpart.strategy.realistic;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticPrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticSecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.HandleStrategy;

public class RealisticHandleStrategy extends RealisticDuoMaterialToolPartStrategy implements HandleStrategy {
    public RealisticHandleStrategy(RealisticPrimaryMaterial material, RealisticSecondaryMaterial secondaryMaterial) {
        super(material, secondaryMaterial);
    }
}
