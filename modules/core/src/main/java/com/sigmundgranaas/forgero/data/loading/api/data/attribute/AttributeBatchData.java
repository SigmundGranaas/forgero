package com.sigmundgranaas.forgero.data.loading.api.data.attribute;

import com.sigmundgranaas.forgero.core.condition.api.Condition;

import java.util.Map;
import java.util.Optional;

/**
 * DTO for attribute batches, a compact JSON syntax for defining multiple attributes
 * with shared conditions and computation settings.
 *
 * <p>Attribute batches allow you to define multiple attributes in a compact map format
 * where they all share the same condition and default computation settings. This reduces
 * verbosity when multiple attributes have identical condition structures.</p>
 *
 * <p>Example JSON usage:</p>
 * <pre>
 * {
 *   "attribute_batches": [
 *     {
 *       "id_prefix": "forgero:iron",
 *       "condition": {
 *         "static": [
 *           {"type": "forgero:in_slot_type", "slot_type": "forgero:tool_material"},
 *           {"type": "forgero:has_other_contributor"}
 *         ]
 *       },
 *       "computation": {
 *         "operator": "add",
 *         "order": "base"
 *       },
 *       "values": {
 *         "forgero:attack_speed": 2.0,
 *         "forgero:attack_damage": 4.0,
 *         "forgero:mining_speed": 6.0
 *       }
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>This expands into individual {@link AttributeData} instances during deserialization,
 * with automatic injection of attribute types into {@code has_other_contributor} conditions.</p>
 *
 * @param idPrefix    Optional prefix for generated attribute IDs. Format: "{prefix}-{attribute_name}"
 * @param condition   Optional shared condition applied to all attributes in the batch
 * @param computation Optional default computation settings (operator, order) for all attributes
 * @param values      Map of attribute type to value or full computation data
 */
public interface AttributeBatchData {
	Optional<String> idPrefix();
	Optional<Condition> condition();
	Optional<ComputationData> computation();
	Map<String, AttributeValueData> values();
}
