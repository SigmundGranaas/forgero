package com.sigmundgranaas.forgero.data.mapper.api;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionMapper;
import com.sigmundgranaas.forgero.data.loading.impl.codec.OperatorMapper;

import java.util.List;
import java.util.Map;

/**
 * A self-contained builder responsible for converting the raw JSON representation of a specific
 * property type into a list of runtime {@link Property} objects.
 * <p>
 * Each implementation of this interface acts as an expert for a single property type,
 * encapsulating all parsing and mapping logic. It knows how to extract its relevant data
 * from the consolidated `properties` map.
 */
public interface PropertyBuilder {
	/**
	 * @return The unique key for the property type this builder handles (e.g., "forgero:attributes").
	 */
	String getPropertyType();

	/**
	 * Builds a list of {@link Property} objects from the consolidated properties map.
	 * The implementation is responsible for finding and parsing its specific data within this map.
	 *
	 * @param properties      The consolidated map of all properties from a definition.
	 * @param conditionMapper A utility to map condition DTOs to runtime Condition objects.
	 * @param operatorMapper  A utility to map operator strings to runtime Operator objects.
	 * @return A list of constructed {@link Property} objects. The list can be empty if the data is invalid or results in no properties.
	 */
	List<Property> build(Map<String, JsonElement> properties, ConditionMapper conditionMapper, OperatorMapper operatorMapper);
}
