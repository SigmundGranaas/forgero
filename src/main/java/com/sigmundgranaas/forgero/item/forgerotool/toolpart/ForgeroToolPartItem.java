package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.ForgeroItem;
import com.sigmundgranaas.forgero.item.forgerotool.ForgeroItemTypes;
import com.sigmundgranaas.forgero.item.forgerotool.model.ToolPartModelType;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.Optional;

public class ForgeroToolPartItem extends Item implements ForgeroItem {
    private final ToolMaterial material;
    private final ForgeroToolPartTypes type;

    public ForgeroToolPartItem(Settings settings, ToolMaterial material, ForgeroToolPartTypes type) {
        super(settings);
        this.material = material;
        this.type = type;
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
        return this.material.toString().toLowerCase(Locale.ROOT) + "_" + getToolPartTypeLowerCase();
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
