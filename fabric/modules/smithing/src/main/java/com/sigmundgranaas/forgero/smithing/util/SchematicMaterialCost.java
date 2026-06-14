package com.sigmundgranaas.forgero.smithing.util;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.util.Identifier;

public final class SchematicMaterialCost {
	public static final String MATERIAL_COST_KEY = "forgero_material_cost";

	public static final String INGREDIENT_COUNT_KEY = "ingredient_count";

	public static final int DEFAULT_MATERIAL_COST = 1;

	/*
	 * Keep this at 3 for now because your anvil currently only lets players stack
	 * up to 3 ingots before choosing a schematic.
	 *
	 * If a future schematic has ingredient_count = 4, also increase the anvil cap.
	 */
	public static final int MAX_MATERIAL_COST = 3;

	private SchematicMaterialCost() {
	}

	public static int getCost(Identifier productId) {
		if (productId == null) {
			return DEFAULT_MATERIAL_COST;
		}

		for (Identifier schematicId : candidateSchematicIds(productId)) {
			Optional<Integer> cost = getCostFromSchematicState(schematicId);

			if (cost.isPresent()) {
				return clampCost(cost.get());
			}
		}

		return DEFAULT_MATERIAL_COST;
	}

	private static List<Identifier> candidateSchematicIds(Identifier productId) {
		String namespace = productId.getNamespace();
		String path = productId.getPath();

		return List.of(
				new Identifier(namespace, path + "-schematic"),
				new Identifier(namespace, path + "_schematic"),
				new Identifier(namespace, path)
		);
	}

	private static Optional<Integer> getCostFromSchematicState(Identifier schematicId) {
		try {
			var maybeState = StateService.INSTANCE.find(schematicId.toString());

			if (maybeState.isEmpty()) {
				return Optional.empty();
			}

			State state = maybeState.get();

			return state.customData()
					.getInteger(INGREDIENT_COUNT_KEY)
					.map(SchematicMaterialCost::toInt);
		} catch (Throwable ignored) {
			return Optional.empty();
		}
	}

	private static int toInt(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}

		if (value instanceof String string) {
			try {
				return Integer.parseInt(string);
			} catch (NumberFormatException ignored) {
				return DEFAULT_MATERIAL_COST;
			}
		}

		return DEFAULT_MATERIAL_COST;
	}

	private static int clampCost(int cost) {
		if (cost < 1) {
			return DEFAULT_MATERIAL_COST;
		}

		return Math.min(cost, MAX_MATERIAL_COST);
	}
}
