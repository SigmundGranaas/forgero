package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.predicate.HasOtherContributorCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeBatchData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeBatchDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeValueData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Codec for {@link AttributeBatchData} that expands batches into individual {@link AttributeData} instances.
 *
 * <p>This codec provides a compact JSON syntax for defining multiple attributes that share
 * the same conditions and computation settings. It automatically handles:</p>
 * <ul>
 *   <li>Expanding value maps into individual attribute definitions</li>
 *   <li>Auto-populating {@code attribute_type} in {@code has_other_contributor} conditions</li>
 *   <li>Supporting per-value computation overrides</li>
 *   <li>Generating attribute IDs from prefixes and attribute type names</li>
 * </ul>
 */
public class AttributeBatchCodecs {

	/**
	 * Creates a codec that parses attribute batches and expands them into a flat list of attributes.
	 *
	 * @param conditionCodec The condition codec for parsing condition structures
	 * @return A codec that produces a list of expanded AttributeData from batches
	 */
	public static Codec<List<AttributeData>> createBatchListCodec(Codec<Condition> conditionCodec) {
		Codec<AttributeBatchData> batchCodec = createBatchCodec(conditionCodec);
		return batchCodec.listOf().xmap(
				AttributeBatchExpander::expandBatches,
				batches -> {
					throw new UnsupportedOperationException("Encoding attribute batches is not supported");
				}
		);
	}

	/**
	 * Codec for batch-level computation that allows value to be omitted.
	 * The value comes from individual attributes in the values map.
	 */
	private static final Codec<ComputationData> BATCH_COMPUTATION_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.FLOAT.optionalFieldOf("value", 0.0f).forGetter(ComputationData::value),
					Codec.STRING.optionalFieldOf("operator", AttributeCodecs.ADDITION_OPERATOR).forGetter(ComputationData::operator),
					Codec.STRING.optionalFieldOf("order", AttributeCodecs.BASE_ORDER).forGetter(ComputationData::order)
			).apply(instance, ComputationData::new));

	/**
	 * Creates a codec for a single attribute batch.
	 */
	private static Codec<AttributeBatchData> createBatchCodec(Codec<Condition> conditionCodec) {
		// Codec for attribute values: either a simple float or a full computation object
		Codec<AttributeValueData> valueCodec = Codec.either(
				Codec.FLOAT,
				AttributeCodecs.COMPUTATION_CODEC
		).xmap(
				either -> either.map(
						AttributeValueData::simple,
						AttributeValueData::withComputation
				),
				val -> val.computationOverride().isPresent()
						? Either.right(val.computationOverride().get())
						: Either.left(val.value())
		);

		return RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.optionalFieldOf("id_prefix").forGetter(AttributeBatchData::idPrefix),
						conditionCodec.optionalFieldOf("condition").forGetter(AttributeBatchData::condition),
						BATCH_COMPUTATION_CODEC.optionalFieldOf("computation").forGetter(AttributeBatchData::computation),
						Codec.unboundedMap(Codec.STRING, valueCodec).fieldOf("values").forGetter(AttributeBatchData::values)
				).apply(instance, AttributeBatchDataImpl::new)
		);
	}

	/**
	 * Expands a list of attribute batches into a flat list of individual attributes.
	 */
	private static class AttributeBatchExpander {

		static List<AttributeData> expandBatches(List<AttributeBatchData> batches) {
			return batches.stream()
					.flatMap(batch -> expandBatch(batch).stream())
					.toList();
		}

		static List<AttributeData> expandBatch(AttributeBatchData batch) {
			return batch.values().entrySet().stream()
					.map(entry -> expandAttribute(batch, entry.getKey(), entry.getValue()))
					.toList();
		}

		static AttributeData expandAttribute(AttributeBatchData batch, String attributeType, AttributeValueData valueData) {
			OpenIdentifier attrTypeId = CodecConstants.IDENTIFIER_FACTORY.of(attributeType);

			// Generate ID from prefix and attribute type
			// If no prefix, use just the simple name
			Optional<OpenIdentifier> id = Optional.of(
					batch.idPrefix()
							.map(prefix -> CodecConstants.IDENTIFIER_FACTORY.of(prefix + "-" + extractSimpleName(attributeType)))
							.orElse(CodecConstants.IDENTIFIER_FACTORY.of(extractSimpleName(attributeType)))
			);

			// Determine computation:
			// - If value has a computation override, use it entirely
			// - Otherwise, use batch's default computation for operator/order, but always use valueData.value()
			ComputationData computation;
			if (valueData.computationOverride().isPresent()) {
				// Use the override's value, operator, and order
				computation = valueData.computationOverride().get();
			} else {
				// Use batch's default operator/order (or fallback), but valueData's value
				String operator = batch.computation()
						.map(ComputationData::operator)
						.orElse(AttributeCodecs.ADDITION_OPERATOR);
				String order = batch.computation()
						.map(ComputationData::order)
						.orElse(AttributeCodecs.BASE_ORDER);

				computation = new ComputationData(valueData.value(), operator, order);
			}

			// Expand condition with attribute_type injection
			Optional<Condition> expandedCondition = batch.condition()
					.map(cond -> injectAttributeType(cond, attrTypeId));

			// Batch attributes inherit context from the batch (default: empty)
			return new AttributeDataImpl(id, attrTypeId, computation, Optional.empty(), expandedCondition);
		}

		/**
		 * Extracts the simple name from a namespaced identifier.
		 * Example: "forgero:attack_speed" -> "attack_speed"
		 */
		private static String extractSimpleName(String identifier) {
			int colonIndex = identifier.indexOf(':');
			return colonIndex >= 0 ? identifier.substring(colonIndex + 1) : identifier;
		}

		/**
		 * Recursively injects attribute_type into has_other_contributor conditions.
		 *
		 * <p>This method walks the condition tree and finds any {@link HasOtherContributorCondition}
		 * instances. When found, it creates a new condition with the attribute type populated.</p>
		 *
		 * @param condition     The condition to process
		 * @param attributeType The attribute type to inject
		 * @return A new condition with attribute types injected
		 */
		private static Condition injectAttributeType(Condition condition, OpenIdentifier attributeType) {
			// Process static conditions
			var newStaticConditions = condition.staticConditions().stream()
					.map(staticCond -> {
						// Check if this is a has_other_contributor condition
						if (staticCond instanceof HasOtherContributorCondition hasOtherCond) {
							// Create a new instance with the attribute type populated
							return new HasOtherContributorCondition(
									hasOtherCond.type(),
									attributeType
							);
						}
						return staticCond;
					})
					.toList();

			// Dynamic conditions pass through unchanged
			var dynamicConditions = condition.dynamicConditions();

			// Create new condition with updated static conditions
			return new Condition(newStaticConditions, dynamicConditions);
		}
	}
}
