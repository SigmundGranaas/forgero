package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.HeadGem;
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
    public ToolPartBuilder setGem(Gem newGem) {
        if (newGem instanceof HeadGem) {
            super.gem = newGem;
        }
        return this;
    }

    @Override
    public AbstractToolPartHead createToolPart() {
        HeadGem headGem;
        if (gem instanceof HeadGem) {
            headGem = (HeadGem) gem;
        } else {
            headGem = EmptyGem.createEmptyGem();
        }
        HeadState state = new HeadState(primary, secondary, headGem, head);
        return switch (head) {
            case PICKAXE -> new PickaxeHead(state);
            case SHOVEL -> new ShovelHead(state);
            case AXE -> new AxeHead(state);
            case SWORD -> null;
        };
    }
}
