package com.sigmundgranaas.forgerocore.deprecated;

import com.sigmundgranaas.forgerocore.schematic.HeadSchematic;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;

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
    FULL_HANDLE,
    MEDIUM_HANDLE,
    SHORT_HANDLE,

    BINDING,
    PICKAXE_BINDING,
    SWORD_BINDING,
    HOE_BINDING,
    AXE_BINDING,
    SHOVEL_BINDING;


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
                case HANDLE -> FULL_HANDLE;
                case BINDING -> PICKAXE_BINDING;
            };
            case AXE -> switch (part) {
                case HEAD -> AXEHEAD;
                case HANDLE -> MEDIUM_HANDLE;
                case BINDING -> AXE_BINDING;
            };
            case SHOVEL -> switch (part) {
                case HEAD -> SHOVELHEAD;
                case HANDLE -> MEDIUM_HANDLE;
                case BINDING -> SHOVEL_BINDING;
            };
            case SWORD -> switch (part) {
                case HEAD -> SWORDHEAD;
                case HANDLE -> SHORT_HANDLE;
                case BINDING -> SWORD_BINDING;
            };
            case HOE -> switch (part) {
                case HEAD -> HOEHEAD;
                case HANDLE -> FULL_HANDLE;
                case BINDING -> HOE_BINDING;
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
        if (identifier.length == 2) {
            for (ToolPartModelType value : ToolPartModelType.values()) {
                if (value.name().toLowerCase().contains(identifier[1].toLowerCase(Locale.ROOT))) {
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