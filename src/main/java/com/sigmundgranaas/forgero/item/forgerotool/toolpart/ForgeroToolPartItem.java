package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.ForgeroItem;
import com.sigmundgranaas.forgero.item.forgerotool.ForgeroItemTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class ForgeroToolPartItem extends Item implements ForgeroItem {
    private ToolMaterial material;
    private ForgeroToolPartTypes type;

    public ForgeroToolPartItem(Settings settings, ToolMaterial material, ForgeroToolPartTypes type) {
        super(settings);
        this.material = material;
        this.type = type;
    }

    public ToolMaterial getMaterial() {
        return material;
    }

    public ForgeroToolPartTypes getToolPartType() {
        return type;
    }

    public String getToolPartTypeLowerCase() {
        return type.toString().toLowerCase(Locale.ROOT);
    }

    public String getToolPartTypeAndMaterialLowerCase() {
        return this.material.toString().toLowerCase(Locale.ROOT) + "_" + type.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public ForgeroItemTypes getItemType() {
        return ForgeroItemTypes.TOOL_PART;
    }

    @Override
    public String getToolTip() {
        return getMaterial().toString() + "_" + getToolPartType();
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(Forgero.MOD_NAMESPACE, getMaterial().toString().toLowerCase(Locale.ROOT) + "_" + getToolPartTypeLowerCase());
    }
}
