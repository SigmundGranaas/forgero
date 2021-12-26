package com.sigmundgranaas.forgero.item.toolpart;

import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import net.minecraft.util.Identifier;

public interface ForgeroToolPartItem {
    public static final String PRIMARY_MATERIAL_IDENTIFIER = "PrimaryMaterial";
    public static final String SECONDARY_MATERIAL_IDENTIFIER = "SecondaryMaterial";
    public static final String MODIFIER_IDENTIFIER = "ForgeroModifier";
    public static final String FORGERO_TOOL_PART_IDENTIFIER = "ForgeroToolPart";
    public static final String FORGERO_TOOL_PART_TYPE_IDENTIFIER = "ForgeroToolPartType";
    public static final String HEAD_IDENTIFIER = "Head";
    public static final String HANDLE_IDENTIFIER = "Handle";
    public static final String BINDING_IDENTIFIER = "Binding";

    Identifier getIdentifier();

    ForgeroMaterial getPrimaryMaterial();

    ForgeroToolPartTypes getType();

    ForgeroToolPart getPart();
}
