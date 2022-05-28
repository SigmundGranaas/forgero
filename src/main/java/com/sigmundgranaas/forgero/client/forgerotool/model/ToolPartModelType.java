package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;

import java.util.Locale;

/**
 * Enum containing all possible model types. Each model should have a corresponding templateTexture
 */
public enum ToolPartModelType {
    PICKAXEHEAD,
    SHOVELHEAD,
    AXEHEAD,
    SWORDHEAD,
    HOEHEAD,


    HANDLE,
    FULLHANDLE,
    MEDIUMHANDLE,
    SHORTHANDLE,

    BINDING,
    PICKAXEBINDING,
    SWORDBINDING,
    HOEBINDING,
    AXEHEADBINDING,
    SHOVELBINDING;


    public static ToolPartModelType getModelType(ForgeroToolPart toolPart) {
        return switch (toolPart.getToolPartType()) {
            case HEAD -> switch (((ToolPartHead) toolPart).getToolType()) {
                case PICKAXE -> PICKAXEHEAD;
                case SHOVEL -> SHOVELHEAD;
                case AXE -> AXEHEAD;
                case SWORD -> SWORDHEAD;
                case HOE -> HOEHEAD;
            };
            case HANDLE -> HANDLE;
            case BINDING -> BINDING;
        };
    }

    public static ToolPartModelType getModelType(Schematic schematic) {
        return switch (schematic.getType()) {
            case HEAD -> switch (((HeadSchematic) schematic).getToolType()) {
                case PICKAXE -> PICKAXEHEAD;
                case SHOVEL -> SHOVELHEAD;
                case AXE -> AXEHEAD;
                case SWORD -> SWORDHEAD;
                case HOE -> HOEHEAD;
            };
            case HANDLE -> HANDLE;
            case BINDING -> BINDING;
        };
    }


    public static ToolPartModelType getModelType(ForgeroToolPart toolPart, ForgeroToolTypes toolType) {
        return getModelType(toolType, toolPart.getToolPartType());
    }

    public static ToolPartModelType getModelType(ForgeroToolTypes toolType, ForgeroToolPartTypes part) {
        return switch (toolType) {
            case PICKAXE -> switch (part) {
                case HEAD -> PICKAXEHEAD;
                case HANDLE -> FULLHANDLE;
                case BINDING -> PICKAXEBINDING;
            };
            case AXE -> switch (part) {
                case HEAD -> AXEHEAD;
                case HANDLE -> MEDIUMHANDLE;
                case BINDING -> AXEHEADBINDING;
            };
            case SHOVEL -> switch (part) {
                case HEAD -> SHOVELHEAD;
                case HANDLE -> MEDIUMHANDLE;
                case BINDING -> SHOVELBINDING;
            };
            case SWORD -> switch (part) {
                case HEAD -> SWORDHEAD;
                case HANDLE -> SHORTHANDLE;
                case BINDING -> SWORDBINDING;
            };
            case HOE -> switch (part) {
                case HEAD -> HOEHEAD;
                case HANDLE -> FULLHANDLE;
                case BINDING -> HOEBINDING;
            };
        };
    }

    public static boolean isModelIdentifier(String[] identifier) {
        if (identifier.length > 1) {
            for (ToolPartModelType value : ToolPartModelType.values()) {
                if (value.name().toLowerCase().equals(identifier[1].toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isItemModelIdentifier(String[] identifier) {
        if (identifier.length == 3) {
            for (ToolPartModelType value : ToolPartModelType.values()) {
                if (value.name().toLowerCase().equals(identifier[1].toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toFileName() {
        return this.toString().toLowerCase(Locale.ROOT);
    }
}