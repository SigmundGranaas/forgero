package com.sigmundgranaas.forgero.core.tool;

public enum ForgeroToolTypes {
    PICKAXE,
    SHOVEL,
    SWORD;

    public static boolean isTool(String element) {
        for (ForgeroToolTypes type : ForgeroToolTypes.values()) {
            if (type.toString().equals(element.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
