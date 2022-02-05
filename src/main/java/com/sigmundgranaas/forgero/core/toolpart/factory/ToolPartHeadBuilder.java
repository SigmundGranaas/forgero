package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.head.*;

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
        HeadState state = new HeadState(primary, secondary, EmptyGem.createEmptyGem(), head);
        return switch (head) {
            case PICKAXE -> new PickaxeHead(state);
            case SHOVEL -> new ShovelHead(state);
            case SWORD -> null;
        };
    }
}
