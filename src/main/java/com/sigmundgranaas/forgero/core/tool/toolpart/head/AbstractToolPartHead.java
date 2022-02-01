package com.sigmundgranaas.forgero.core.tool.toolpart.head;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.ToolPartDescriptionWriter;
import com.sigmundgranaas.forgero.core.tool.toolpart.AbstractToolPart;

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

    @Override
    public int getMiningLevel() {
        int level = 1;
        if (getSecondaryMaterial() instanceof EmptySecondaryMaterial) {
            return level;
        } else {
            return level + 1;
        }
    }

    @Override
    public void createToolPartDescription(ToolPartDescriptionWriter writer) {
        super.createToolPartDescription(writer);
        writer.addMiningLevel(getMiningLevel());
    }
}
