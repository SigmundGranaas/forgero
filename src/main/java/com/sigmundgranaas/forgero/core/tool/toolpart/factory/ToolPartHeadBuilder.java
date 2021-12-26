package com.sigmundgranaas.forgero.core.tool.toolpart.factory;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.AbstractToolPartHead;
import com.sigmundgranaas.forgero.core.tool.toolpart.PickaxeHead;

public class ToolPartHeadBuilder extends ToolPartBuilder {
    private final ForgeroToolTypes head;

    public ToolPartHeadBuilder(PrimaryMaterial primary, ForgeroToolTypes head) {
        super(primary);
        this.head = head;
    }

    public ForgeroToolTypes getHead() {
        return head;
    }

    @Override
    public AbstractToolPartHead createToolPart() {
        return switch (head) {
            case PICKAXE -> new PickaxeHead(this);
            case SHOVEL -> null;
            case SWORD -> null;
        };
    }
}
