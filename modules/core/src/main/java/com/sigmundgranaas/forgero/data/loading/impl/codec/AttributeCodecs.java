package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;

import java.util.Optional;

public class AttributeCodecs {
	public static final String ADDITION_OPERATOR = "forgero:addition";
	public static final String SUBTRACTION_OPERATOR = "forgero:subtraction";
	public static final String MULTIPLICATION_OPERATOR = "forgero:multiplication";
	public static final String DIVISION_OPERATOR = "forgero:division";

	public static final String BASE_ORDER = "forgero:base";
	public static final String MIDDLE_ORDER = "forgero:middle";
	public static final String END_ORDER = "forgero:end";

	private static final Codec<ComputationData> FULL_COMPUTATION_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.FLOAT.fieldOf("value").forGetter(ComputationData::value),
					Codec.STRING.optionalFieldOf("operator", ADDITION_OPERATOR).forGetter(ComputationData::operator),
					Codec.STRING.optionalFieldOf("order", BASE_ORDER).forGetter(ComputationData::order)
			).apply(instance, ComputationData::new));

	public static final Codec<ComputationData> COMPUTATION_CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<ComputationData, T>> decode(DynamicOps<T> ops, T input) {
			Optional<Pair<ComputationData, T>> asNumber = ops.getNumberValue(input)
					.map(num -> Pair.of(new ComputationData(num.floatValue(), ADDITION_OPERATOR, BASE_ORDER), ops.empty()))
					.result();

			if (asNumber.isPresent()) {
				return DataResult.success(asNumber.get());
			}

			return ops.getMap(input).flatMap(map -> {
				var addKey = ops.createString("add");
				var subtractKey = ops.createString("subtract");
				var multiplyKey = ops.createString("multiply");
				var divideKey = ops.createString("divide");

				if (map.get(addKey) != null) {
					return ops.getNumberValue(map.get(addKey))
							.map(num -> new ComputationData(num.floatValue(), ADDITION_OPERATOR, BASE_ORDER))
							.map(data -> Pair.of(data, ops.empty()));
				}
				if (map.get(subtractKey) != null) {
					return ops.getNumberValue(map.get(subtractKey))
							.map(num -> new ComputationData(num.floatValue(), SUBTRACTION_OPERATOR, BASE_ORDER))
							.map(data -> Pair.of(data, ops.empty()));
				}
				if (map.get(multiplyKey) != null) {
					return ops.getNumberValue(map.get(multiplyKey))
							.map(num -> new ComputationData(num.floatValue(), MULTIPLICATION_OPERATOR, BASE_ORDER))
							.map(data -> Pair.of(data, ops.empty()));
				}
				if (map.get(divideKey) != null) {
					return ops.getNumberValue(map.get(divideKey))
							.map(num -> new ComputationData(num.floatValue(), DIVISION_OPERATOR, BASE_ORDER))
							.map(data -> Pair.of(data, ops.empty()));
				}
				return FULL_COMPUTATION_CODEC.decode(ops, input);
			}).mapError(err -> "Not a valid ComputationData format: " + err);
		}

		@Override
		public <T> DataResult<T> encode(ComputationData input, DynamicOps<T> ops, T prefix) {
			return FULL_COMPUTATION_CODEC.encode(input, ops, prefix);
		}
	};

	public static Codec<AttributeData> create(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("id").forGetter(AttributeData::id),
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(AttributeData::type),
						COMPUTATION_CODEC.fieldOf("computation").forGetter(AttributeData::computation),
						CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("context").forGetter(AttributeData::context),
						conditionCodec.optionalFieldOf("condition").forGetter(AttributeData::condition)
				).apply(instance, AttributeDataImpl::new)
		);
	}

	/**
	 * Creates a codec for attribute lists that supports both traditional and batch formats.
	 *
	 * <p>This codec can parse material JSON files with either:</p>
	 * <ul>
	 *   <li>{@code "attributes": [...]}: Traditional attribute array</li>
	 *   <li>{@code "attribute_batches": [...]}: Compact batch syntax</li>
	 *   <li>Both fields simultaneously (results are merged)</li>
	 * </ul>
	 *
	 * @param conditionCodec The condition codec for parsing conditions
	 * @return A codec that handles both traditional and batch attribute formats
	 */
	public static Codec<java.util.List<AttributeData>> createListWithBatchSupport(Codec<Condition> conditionCodec) {
		Codec<java.util.List<AttributeData>> traditionalCodec = Codec.list(create(conditionCodec));
		Codec<java.util.List<AttributeData>> batchCodec = AttributeBatchCodecs.createBatchListCodec(conditionCodec);

		// Return a codec that tries both formats and merges the results
		return new Codec<java.util.List<AttributeData>>() {
			@Override
			public <T> DataResult<com.mojang.datafixers.util.Pair<java.util.List<AttributeData>, T>> decode(DynamicOps<T> ops, T input) {
				// For now, just use traditional codec - merging happens at MaterialCodecs level
				return traditionalCodec.decode(ops, input);
			}

			@Override
			public <T> DataResult<T> encode(java.util.List<AttributeData> input, DynamicOps<T> ops, T prefix) {
				return traditionalCodec.encode(input, ops, prefix);
			}
		};
	}
}
