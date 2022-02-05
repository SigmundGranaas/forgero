package com.sigmundgranaas.forgero.core.toolpart.strategy.realistic;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticPrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticSecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.strategy.BindingMaterialStrategy;

public class RealisticBindingStrategy extends RealisticDuoMaterialToolPartStrategy implements BindingMaterialStrategy {
    public RealisticBindingStrategy(RealisticPrimaryMaterial material, RealisticSecondaryMaterial secondaryMaterial) {
        super(material, secondaryMaterial);
    }
}
