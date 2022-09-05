package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgerocore.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.property.PropertyContainer;
import com.sigmundgranaas.forgerocore.resource.ForgeroResourceType;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface ToolPartItem extends ForgeroItem<Item, ToolPartPojo>, PropertyContainer {
    String PRIMARY_MATERIAL_IDENTIFIER = "PrimaryMaterial";
    String SECONDARY_MATERIAL_IDENTIFIER = "SecondaryMaterial";
    String GEM_IDENTIFIER = "ForgeroModifier";
    String FORGERO_TOOL_PART_IDENTIFIER = "ForgeroToolPart";
    String FORGERO_TOOL_PART_TYPE_IDENTIFIER = "ForgeroToolPartType";
    String HEAD_IDENTIFIER = "Head";
    String HANDLE_IDENTIFIER = "Handle";
    String BINDING_IDENTIFIER = "Binding";

    Identifier getIdentifier();

    PrimaryMaterial getPrimaryMaterial();

    ForgeroToolPartTypes getType();

    ForgeroToolPart getPart();

    Item getItem();

    @Override
    default String getStringIdentifier() {
        return getIdentifier().getPath();
    }

    @Override
    default String getResourceName() {
        return getStringIdentifier();
    }

    @Override
    default ForgeroResourceType getResourceType() {
        return ForgeroResourceType.TOOL_PART;
    }

    @Override
    default int compareTo(@NotNull Object o) {
        int containerResult = PropertyContainer.super.compareTo(o);
        if (containerResult != 0) {
            return containerResult;
        } else {
            return ForgeroItem.super.compareTo(o);
        }
    }
}
