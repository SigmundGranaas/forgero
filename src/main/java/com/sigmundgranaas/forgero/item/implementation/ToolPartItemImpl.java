package com.sigmundgranaas.forgero.item.implementation;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class ToolPartItemImpl extends Item implements ToolPartItem {
    private final PrimaryMaterial material;
    private final ForgeroToolPartTypes type;
    private final ForgeroToolPart part;

    public ToolPartItemImpl(Settings settings, PrimaryMaterial material, ForgeroToolPartTypes type, ForgeroToolPart part) {
        super(settings);
        this.material = material;
        this.type = type;
        this.part = part;
    }

    public ForgeroToolPartTypes getToolPartType() {
        return type;
    }

    public String getToolPartIdentifierString() {
        return part.getToolPartIdentifier();
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(Forgero.MOD_NAMESPACE, getToolPartIdentifierString());
    }

    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return material;
    }

    @Override
    public ForgeroToolPartTypes getType() {
        return type;
    }

    @Override
    public ForgeroToolPart getPart() {
        return part;
    }


}
