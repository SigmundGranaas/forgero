package com.sigmundgranaas.forgero.smithing.util;

import net.minecraft.util.Identifier;

public final class SchematicMaterialCost {
	public static final String MATERIAL_COST_KEY = "forgero_material_cost";
	public static final int MAX_MATERIAL_COST = 3;

	private SchematicMaterialCost() {
	}

	public static int getCost(Identifier productId) {
		if (productId == null) {
			return 1;
		}

		String path = productId.getPath();

		if (path.contains("pickaxe") || path.contains("axe")) {
			return 3;
		}

		if (path.contains("hoe") || path.contains("sword") || path.contains("blade")) {
			return 2;
		}

		if (path.contains("shovel")) {
			return 1;
		}

		return 1;
	}
}
