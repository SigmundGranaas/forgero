package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import net.minecraft.util.Identifier;

public interface ToolPartItem {
    String PRIMARY_MATERIAL_IDENTIFIER = "PrimaryMaterial";
    String SECONDARY_MATERIAL_IDENTIFIER = "SecondaryMaterial";
    String MODIFIER_IDENTIFIER = "ForgeroModifier";
    String FORGERO_TOOL_PART_IDENTIFIER = "ForgeroToolPart";
    String FORGERO_TOOL_PART_TYPE_IDENTIFIER = "ForgeroToolPartType";
    String HEAD_IDENTIFIER = "Head";
    String HANDLE_IDENTIFIER = "Handle";
    String BINDING_IDENTIFIER = "Binding";

    Identifier getIdentifier();

    PrimaryMaterial getPrimaryMaterial();

    ForgeroToolPartTypes getType();

    ForgeroToolPart getPart();
}
