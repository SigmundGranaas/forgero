package com.sigmundgranaas.forgerocore.recipe;

public class IngredientType {
    public static IngredientType TYPED_INGREDIENT = new IngredientType("TYPED");
    public static IngredientType IDENTIFIED_INGREDIENT = new IngredientType("IDENTIFIED");
    private String type;

    public IngredientType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
