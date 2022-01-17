package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;

public abstract class AbstractToolPartHead extends AbstractToolPart implements ToolPartHead {
    private final ForgeroToolTypes head;

    public AbstractToolPartHead(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial, ForgeroToolTypes type) {
        super(primaryMaterial, secondaryMaterial);
        this.head = type;
    }

    public AbstractToolPartHead(PrimaryMaterial primaryMaterial, ForgeroToolTypes type) {
        super(primaryMaterial);
        this.head = type;
    }

    @Override
    public ForgeroToolTypes getHeadType() {
        return head;
    }
}
