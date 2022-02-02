package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.head.ToolPartHead;

import java.util.Locale;

/**
 * Enum containing all possible model types. Each model should have a corresponding templateTexture
 */
public enum ToolPartModelType {
    PICKAXEHEAD,
    SHOVELHEAD,
    AXEHEAD,
    FULLHANDLE,
    MEDIUMHANDLE,
    SHORTHANDLE,
    PICKAXEBINDING,
    HANDLE,
    BINDING,
    SHOVELBINDING;

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static ToolPartModelType getModelType(ForgeroToolPart toolPart) {
        return switch (toolPart.getToolPartType()) {
            case HEAD -> switch (((ToolPartHead) toolPart).getToolType()) {
                case PICKAXE -> PICKAXEHEAD;
                case SHOVEL -> SHOVELHEAD;
                case SWORD -> PICKAXEHEAD;
            };
            case HANDLE -> HANDLE;
            case BINDING -> BINDING;
        };
    }

    public static ToolPartModelType getModelType(ForgeroToolPart toolPart, ForgeroToolTypes toolType) {
        return switch (toolType) {
            case PICKAXE -> switch (toolPart.getToolPartType()) {
                case HEAD -> PICKAXEHEAD;
                case HANDLE -> FULLHANDLE;
                case BINDING -> PICKAXEBINDING;
            };
            case SHOVEL -> switch (toolPart.getToolPartType()) {
                case HEAD -> SHOVELHEAD;
                case HANDLE -> MEDIUMHANDLE;
                case BINDING -> SHOVELBINDING;
            };
            case SWORD -> PICKAXEHEAD;
        };
    }

    public String toFileName() {
        return this.toString().toLowerCase(Locale.ROOT);
    }
}