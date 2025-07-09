package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.feature.impl.FeatureEngine;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyBuilder;
import com.sigmundgranaas.forgero.data.mapper.impl.AttributePropertyBuilder;
import com.sigmundgranaas.forgero.data.mapper.impl.FeaturePropertyBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of the PropertyRegistry.
 * This is a singleton to ensure a single source of truth for registered properties.
 * It pre-registers core Forgero property types (Attributes, Features).
 * <p>
 * Note: The `reset()` method is for testing only. In a real application,
 * this registry is initialized once.
 */
public class DefaultPropertyRegistry implements PropertyRegistry {
	static final DefaultPropertyRegistry INSTANCE = new DefaultPropertyRegistry();

	private final List<PropertyBuilder> propertyBuilders = new ArrayList<>();
	private final List<DataTypeEngine<?, ?>> dataTypeEngines = new ArrayList<>();

	public DefaultPropertyRegistry() {
		// Register core Forgero builders and engines here upon first instantiation.
		// This ensures they are always available.
		reset(); // Call reset to populate initial core components
	}

	@Override
	public void registerPropertyBuilder(PropertyBuilder builder) {
		// Simple check to prevent immediate duplicates by type, but more robust checks can be added
		if (propertyBuilders.stream().noneMatch(b -> b.getPropertyType().equals(builder.getPropertyType()))) {
			this.propertyBuilders.add(builder);
		}
	}

	@Override
	public void registerDataTypeEngine(DataTypeEngine<?, ?> engine) {
		// Simple check to prevent immediate duplicates by key, but more robust checks can be added
		if (dataTypeEngines.stream().noneMatch(e -> e.key().equals(engine.key()))) {
			this.dataTypeEngines.add(engine);
		}
	}

	@Override
	public List<PropertyBuilder> getPropertyBuilders() {
		return Collections.unmodifiableList(propertyBuilders);
	}

	@Override
	public List<DataTypeEngine<?, ?>> getDataTypeEngines() {
		return Collections.unmodifiableList(dataTypeEngines);
	}

	@Override
	public void reset() {
		propertyBuilders.clear();
		dataTypeEngines.clear();
		
		propertyBuilders.add(new AttributePropertyBuilder());
		propertyBuilders.add(new FeaturePropertyBuilder());
		dataTypeEngines.add(new AttributeEngine());
		dataTypeEngines.add(new FeatureEngine());
	}
}
