package com.sigmundgranaas.forgero.core.tool.toolpart.factory;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.AbstractToolPartHead;
import com.sigmundgranaas.forgero.core.tool.toolpart.PickaxeHead;
import com.sigmundgranaas.forgero.core.tool.toolpart.ShovelHead;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;

public class ToolPartHeadBuilder extends ToolPartBuilder {
    private final ForgeroToolTypes head;

    public ToolPartHeadBuilder(PrimaryMaterial primary, ForgeroToolTypes head) {
        super(primary);
        this.head = head;
    }

    public ToolPartHeadBuilder(ToolPartHead toolPart) {
        super(toolPart);
        this.head = toolPart.getHeadType();
    }

    public ForgeroToolTypes getHead() {
        return head;
    }

    @Override
    public AbstractToolPartHead createToolPart() {
        return switch (head) {
            case PICKAXE -> new PickaxeHead(this);
            case SHOVEL -> new ShovelHead(this);
            case SWORD -> null;
        };
    }
}
