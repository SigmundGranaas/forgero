package com.sigmundgranaas.forgero.tool;

public enum ForgeroToolTypes {
    PICKAXE,
    SHOVEL,
    AXE,
    HOE,
    SWORD;

    public static boolean isTool(String element) {
        for (ForgeroToolTypes type : ForgeroToolTypes.values()) {
            if (type.toString().equals(element.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public String getToolName() {
        return this.name().toLowerCase();
    }
}
