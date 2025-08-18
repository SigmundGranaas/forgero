package com.sigmundgranaas.forgero.core.attribute.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.attribute.api.operator.*;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A dispatching codec for the {@link Attribute} interface.
 * It can serialize and deserialize different implementations of Attribute,
 * such as {@link SimpleAttribute} and {@link CompositeAttributeComponent},
 * by inspecting the fields of the data object.
 * <p>
 * - If a "composite_key" field is present, it decodes to a {@link CompositeAttributeComponent}.
 * - Otherwise, it decodes to a {@link SimpleAttribute}.
 * <p>
 * This codec is designed to be a self-contained component for handling attribute serialization,
 * intended for use within Forgero's property system. It does not need to implement any
 * Forgero-specific interfaces itself, making it a clean, standard codec.
 */
public final class AttributeCodec implements Codec<Attribute> {

	private final Codec<SimpleAttribute> simpleAttributeCodec;
	private final Codec<CompositeAttributeComponent> compositeAttributeComponentCodec;

	/**
	 * Constructs an AttributeCodec with a dependency on the master Condition codec.
	 *
	 * @param conditionCodec The master codec for parsing {@link Condition} objects. This should be
	 *                       supplied by the application's codec registry.
	 */
	public AttributeCodec(Codec<Condition> conditionCodec) {
		Codec<Operator> operatorCodec = createOperatorCodec();

		this.simpleAttributeCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.optionalFieldOf("id").forGetter(SimpleAttribute::id),
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(SimpleAttribute::type),
						Codec.FLOAT.fieldOf("value").forGetter(SimpleAttribute::value),
						operatorCodec.fieldOf("operator").orElse(AdditionOperator.getInstance()).forGetter(SimpleAttribute::operator),
						Codec.INT.fieldOf("group").orElse(0).forGetter(SimpleAttribute::group),
						conditionCodec.optionalFieldOf("condition").forGetter(attr -> attr.condition().filter(c -> c != Condition.ALWAYS_TRUE))
				).apply(instance, (id, type, value, operator, group, condition) -> new SimpleAttribute(id, type, value, operator, group, condition.orElse(Condition.ALWAYS_TRUE)))
		);

		this.compositeAttributeComponentCodec = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.optionalFieldOf("id").forGetter(CompositeAttributeComponent::id),
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(CompositeAttributeComponent::type),
						Codec.FLOAT.fieldOf("value").forGetter(CompositeAttributeComponent::value),
						operatorCodec.fieldOf("operator").orElse(AdditionOperator.getInstance()).forGetter(CompositeAttributeComponent::operator),
						Codec.INT.fieldOf("group").orElse(0).forGetter(CompositeAttributeComponent::group),
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("composite_key").forGetter(CompositeAttributeComponent::compositeKey)
				).apply(instance, CompositeAttributeComponent::new)
		);
	}

	@Override
	public <T> DataResult<Pair<Attribute, T>> decode(DynamicOps<T> ops, T input) {
		return ops.getMap(input).flatMap(map -> {
			Optional<T> compositeKey = Optional.ofNullable(map.get(ops.createString("composite_key")));
			if (compositeKey.isPresent()) {
				return compositeAttributeComponentCodec.decode(ops, input).map(pair -> pair.mapFirst(Function.identity()));
			} else {
				return simpleAttributeCodec.decode(ops, input).map(pair -> pair.mapFirst(Function.identity()));
			}
		});
	}

	@Override
	public <T> DataResult<T> encode(Attribute input, DynamicOps<T> ops, T prefix) {
		if (input instanceof CompositeAttributeComponent component) {
			return compositeAttributeComponentCodec.encode(component, ops, prefix);
		} else if (input instanceof SimpleAttribute attribute) {
			return simpleAttributeCodec.encode(attribute, ops, prefix);
		}
		// CompositeAttribute is not meant to be serialized to data files, so it's not handled here.
		return DataResult.error(() -> "Unsupported Attribute type for encoding: " + input.getClass().getName());
	}

	private static Codec<Operator> createOperatorCodec() {
		Map<String, Operator> stringToOp = new HashMap<>();
		stringToOp.put("addition", AdditionOperator.getInstance());
		stringToOp.put("add", AdditionOperator.getInstance());
		stringToOp.put("subtraction", SubtractionOperator.getInstance());
		stringToOp.put("sub", SubtractionOperator.getInstance());
		stringToOp.put("multiplication", MultiplicationOperator.getInstance());
		stringToOp.put("mul", MultiplicationOperator.getInstance());
		stringToOp.put("division", DivisionOperator.getInstance());
		stringToOp.put("div", DivisionOperator.getInstance());
		stringToOp.put("max", MaxOperator.getInstance());
		stringToOp.put("min", MinOperator.getInstance());

		Map<Operator, String> opToString = Map.of(
				AdditionOperator.getInstance(), "add",
				SubtractionOperator.getInstance(), "sub",
				MultiplicationOperator.getInstance(), "mul",
				DivisionOperator.getInstance(), "div",
				MaxOperator.getInstance(), "max",
				MinOperator.getInstance(), "min"
		);

		return Codec.STRING.xmap(
				s -> stringToOp.getOrDefault(s.toLowerCase(), AdditionOperator.getInstance()),
				op -> opToString.getOrDefault(op, "add")
		);
	}
}
