package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;

import java.util.List;
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
			// First, try to decode as a simple number.
			Optional<Pair<ComputationData, T>> asNumber = ops.getNumberValue(input)
					.map(num -> Pair.of(new ComputationData(num.floatValue(), ADDITION_OPERATOR, BASE_ORDER), ops.empty()))
					.result();

			if (asNumber.isPresent()) {
				return DataResult.success(asNumber.get());
			}

			// If not a number, try to decode as a map (object).
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

				// If no shortcuts, fall back to the full, explicit object codec.
				return FULL_COMPUTATION_CODEC.decode(ops, input);
			}).mapError(err -> "Not a valid ComputationData format: " + err);
		}

		@Override
		public <T> DataResult<T> encode(ComputationData input, DynamicOps<T> ops, T prefix) {
			return FULL_COMPUTATION_CODEC.encode(input, ops, prefix);
		}
	};

	public static final Codec<AttributeData> ATTRIBUTE_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("id").forGetter(data -> Optional.ofNullable(data.id())),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(AttributeData::type),
					COMPUTATION_CODEC.fieldOf("computation").forGetter(AttributeData::computation),
					ConditionCodecs.CONDITION_DATA_CODEC.optionalFieldOf("condition").forGetter(data -> Optional.ofNullable(data.condition())),
					CodecConstants.OPEN_IDENTIFIER_CODEC.optionalFieldOf("composite").forGetter(data -> Optional.ofNullable(data.composite()))
			).apply(instance, (idOpt, type, comp, condOpt, compositeOpt) ->
					new AttributeDataImpl(idOpt.orElse(null), type, comp, condOpt.orElse(null), compositeOpt.orElse(null)))
	);


	public static final Codec<List<AttributeData>> ATTRIBUTE_DATA_LIST_CODEC = Codec.list(ATTRIBUTE_DATA_CODEC);
}
