package com.sigmundgranaas.forgero.core.recipe;

public class RecipeType {
	public static RecipeType CRAFTING = new RecipeType("CRAFTING");
	public static RecipeType SMITHING = new RecipeType("SMITHING");
	private String type;

	public RecipeType(String type) {
		this.type = type;
	}

	public String type() {
		return this.type;
	}
}
