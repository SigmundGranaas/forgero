package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;

import java.util.Locale;

public class Handle extends AbstractToolPart {
    public Handle(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial) {
        super(primaryMaterial, secondaryMaterial);
    }

    public Handle(PrimaryMaterial primaryMaterial) {
        super(primaryMaterial);
    }

    @Override
    public String getToolTypeName() {
        return getToolPartName();
    }

    @Override
    public String getToolPartName() {
        return ForgeroToolPartTypes.HANDLE.toString().toLowerCase(Locale.ROOT);
    }
}
