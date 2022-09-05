package com.sigmundgranaas.forgerocore.toolpart.factory;

import com.sigmundgranaas.forgerocore.gem.EmptyGem;
import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.schematic.HeadSchematic;
import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgerocore.toolpart.head.*;

public class ToolPartHeadBuilder extends ToolPartBuilder {
    private final ForgeroToolTypes head;

    public ToolPartHeadBuilder(PrimaryMaterial primary, HeadSchematic pattern) {
        super(primary, pattern);
        this.head = pattern.getToolType();
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
        if (newGem.getPlacement().contains(ForgeroToolPartTypes.HEAD)) {
            super.gem = newGem;
        }
        return this;
    }

    @Override
    public AbstractToolPartHead createToolPart() {
        Gem headGem;
        if (gem.getPlacement().contains(ForgeroToolPartTypes.HEAD)) {
            headGem = gem;
        } else {
            headGem = EmptyGem.createEmptyGem();
        }
        HeadState state = new HeadState(primary, secondary, headGem, schematic);
        return switch (head) {
            case PICKAXE -> new PickaxeHead(state);
            case SHOVEL -> new ShovelHead(state);
            case AXE -> new AxeHead(state);
            case SWORD -> new SwordHead(state);
            case HOE -> new HoeHead(state);
        };
    }
}
