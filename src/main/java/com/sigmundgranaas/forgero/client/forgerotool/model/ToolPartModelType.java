package com.sigmundgranaas.forgero.client.forgerotool.model;

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

    public String toFileName() {
        return this.toString().toLowerCase(Locale.ROOT).replace("_", "");
    }
}
