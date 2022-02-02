package com.sigmundgranaas.forgero.core.tool.toolpart.strategy.realistic;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticPrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticSecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.binding.BindingStrategy;

public class RealisticBindingStrategy extends RealisticDuoMaterialToolPartStrategy implements BindingStrategy {
    public RealisticBindingStrategy(RealisticPrimaryMaterial material, RealisticSecondaryMaterial secondaryMaterial) {
        super(material, secondaryMaterial);
    }
}
