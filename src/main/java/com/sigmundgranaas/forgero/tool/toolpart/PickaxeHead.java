package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;

import java.util.Locale;

public class PickaxeHead extends AbstractToolPart {
    public PickaxeHead(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial) {
        super(primaryMaterial, secondaryMaterial);
    }

    public PickaxeHead(PrimaryMaterial primaryMaterial) {
        super(primaryMaterial);
    }

    @Override
    public String getToolTypeName() {
        return "pickaxe";
    }

    @Override
    public String getToolPartName() {
        return ForgeroToolPartTypes.PICKAXEHEAD.toString().toLowerCase(Locale.ROOT);
    }
}
