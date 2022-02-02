package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.head.AbstractToolPartHead;
import com.sigmundgranaas.forgero.core.toolpart.head.PickaxeHead;
import com.sigmundgranaas.forgero.core.toolpart.head.ShovelHead;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;

public class ToolPartHeadBuilder extends ToolPartBuilder {
    private final ForgeroToolTypes head;

    public ToolPartHeadBuilder(PrimaryMaterial primary, ForgeroToolTypes head) {
        super(primary);
        this.head = head;
    }

    public ToolPartHeadBuilder(ToolPartHead toolPart) {
        super(toolPart);
        this.head = toolPart.getToolType();
    }

    public ForgeroToolTypes getHead() {
        return head;
    }

    @Override
    public AbstractToolPartHead createToolPart() {
        return switch (head) {
            case PICKAXE -> new PickaxeHead(ToolPartStrategyFactory.createToolPartHeadStrategy(head, primary, secondary));
            case SHOVEL -> new ShovelHead(ToolPartStrategyFactory.createToolPartHeadStrategy(head, primary, secondary));
            case SWORD -> null;
        };
    }
}
