package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.model.ModelOverrideBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of ModelOverrideBuilder.
 */
public class ModelOverrideBuilderImpl implements ModelOverrideBuilder {

	private final Map<String, Float> predicates = new HashMap<>();
	private String model;

	@Override
	public ModelOverrideBuilder customModelData(int value) {
		predicates.put("custom_model_data", (float) value);
		return this;
	}

	@Override
	public ModelOverrideBuilder damage(float damage) {
		predicates.put("damage", damage);
		return this;
	}

	@Override
	public ModelOverrideBuilder damaged(boolean damaged) {
		predicates.put("damaged", damaged ? 1f : 0f);
		return this;
	}

	@Override
	public ModelOverrideBuilder pulling(float pulling) {
		predicates.put("pulling", pulling);
		return this;
	}

	@Override
	public ModelOverrideBuilder pull(float pull) {
		predicates.put("pull", pull);
		return this;
	}

	@Override
	public ModelOverrideBuilder predicate(String predicateId, float value) {
		predicates.put(predicateId, value);
		return this;
	}

	@Override
	public ModelOverrideBuilder model(String modelId) {
		this.model = modelId;
		return this;
	}

	@Override
	public Map<String, Float> getPredicates() {
		return Map.copyOf(predicates);
	}

	@Override
	public String getModel() {
		return model;
	}
}
