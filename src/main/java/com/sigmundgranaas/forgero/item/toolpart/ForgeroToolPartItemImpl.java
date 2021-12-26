package com.sigmundgranaas.forgero.item.toolpart;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.item.ForgeroItemTypes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.Optional;

public class ForgeroToolPartItemImpl extends Item implements ForgeroToolPartItem {
    private final PrimaryMaterial material;
    private final ForgeroToolPartTypes type;
    private final ForgeroToolPart part;

    public ForgeroToolPartItemImpl(Settings settings, PrimaryMaterial material, ForgeroToolPartTypes type, ForgeroToolPart part) {
        super(settings);
        this.material = material;
        this.type = type;
        this.part = part;
    }

    public static Optional<ForgeroToolPartTypes> getToolPartTypeFromFileName(String fileName) {
        String[] elements = fileName.split("_");
        assert elements.length == 2;
        String toolpart = elements[1];
        try {
            return Optional.of(ForgeroToolPartTypes.valueOf(toolpart.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            Forgero.LOGGER.warn(e);
            return Optional.empty();
        }
    }

    public static Optional<ToolPartModelType> getToolPartModelTypeFromFileName(String fileName) {
        String[] elements = fileName.split("_");
        assert elements.length == 2;
        String toolpart = elements[1];
        try {
            return Optional.of(ToolPartModelType.valueOf(toolpart.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            Forgero.LOGGER.warn(e);
            return Optional.empty();
        }
    }

    public static Optional<String> getMaterialFromFileName(String fileName) {
        //TODO Add support for all materials???
        String[] elements = fileName.split("_");
        assert elements.length == 2;
        String material = elements[0];
        try {
            return Optional.of(material.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Forgero.LOGGER.warn(e);
            return Optional.empty();
        }
    }

    public ForgeroToolPartTypes getToolPartType() {
        return type;
    }

    public String getToolPartTypeLowerCase() {
        return type.toString().toLowerCase(Locale.ROOT);
    }

    public String getToolPartTypeAndMaterialLowerCase() {
        return this.material.toString().toLowerCase(Locale.ROOT) + "_" + getToolPartTypeLowerCase();
    }


    public ForgeroItemTypes getItemType() {
        return ForgeroItemTypes.TOOL_PART;
    }


    public String getToolTip() {
        return getPrimaryMaterial().getName() + "_" + getToolPartType();
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(Forgero.MOD_NAMESPACE, getPrimaryMaterial().getName() + "_" + getToolPartTypeLowerCase());
    }

    @Override
    public ForgeroMaterial getPrimaryMaterial() {
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
