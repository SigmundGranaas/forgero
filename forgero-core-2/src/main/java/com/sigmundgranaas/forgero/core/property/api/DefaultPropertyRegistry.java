package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.feature.impl.FeatureEngine;
import com.sigmundgranaas.forgero.data.loading.impl.codec.OperatorMapper;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import com.sigmundgranaas.forgero.data.mapper.impl.AttributeCodec;
import com.sigmundgranaas.forgero.data.mapper.impl.FeatureCodec;

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

	private final List<PropertyCodec<?>> propertyCodecs = new ArrayList<>();
	private final List<DataTypeEngine<?, ?>> dataTypeEngines = new ArrayList<>();

	private final OperatorMapper operatorMapper;


	public DefaultPropertyRegistry() {
		// Initialize shared dependencies
		this.operatorMapper = new OperatorMapper();
		reset();
	}

	@Override
	public void registerPropertyCodec(PropertyCodec<?> codec) {
		if (propertyCodecs.stream().noneMatch(c -> c.getPropertyType().equals(codec.getPropertyType()))) {
			this.propertyCodecs.add(codec);
		}
	}

	@Override
	public void registerDataTypeEngine(DataTypeEngine<?, ?> engine) {
		if (dataTypeEngines.stream().noneMatch(e -> e.key().equals(engine.key()))) {
			this.dataTypeEngines.add(engine);
		}
	}

	@Override
	public List<PropertyCodec<?>> getPropertyCodecs() {
		return Collections.unmodifiableList(propertyCodecs);
	}

	@Override
	public List<DataTypeEngine<?, ?>> getDataTypeEngines() {
		return Collections.unmodifiableList(dataTypeEngines);
	}

	@Override
	public void reset() {
		propertyCodecs.clear();
		dataTypeEngines.clear();

		// Register core Forgero codecs and engines here, injecting dependencies.
		registerPropertyCodec(new AttributeCodec(operatorMapper));
		registerPropertyCodec(new FeatureCodec());

		dataTypeEngines.add(new AttributeEngine());
		dataTypeEngines.add(new FeatureEngine());
	}
}
