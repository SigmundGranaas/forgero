package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;

public abstract class AbstractToolPartHead extends AbstractToolPart implements ToolPartHead {
    public AbstractToolPartHead(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial) {
        super(primaryMaterial, secondaryMaterial);
    }

    public AbstractToolPartHead(PrimaryMaterial primaryMaterial) {
        super(primaryMaterial);
    }
}
