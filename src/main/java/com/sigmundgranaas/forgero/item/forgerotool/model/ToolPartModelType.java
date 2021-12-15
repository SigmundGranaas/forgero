package com.sigmundgranaas.forgero.item.forgerotool.model;

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
    SHOVELBINDING,
    BINDING,
    HANDLE;

    public String toFileName() {
        return this.toString().toLowerCase(Locale.ROOT).replace("_", "");
    }
}
