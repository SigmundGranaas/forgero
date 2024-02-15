package com.sigmundgranaas.forgero.core.property.attribute;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.context.Context;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.Nullable;

/**
 * Base attribute class. This class is opinionated when it comes to how some attributes should be calculated, like MINING level.
 * Special attribute classes will likely deal with special scenarios like MINING level.
 */
public record BaseAttribute(String attribute,
                            NumericOperation operation,
                            float value,
                            Matchable condition,
                            CalculationOrder order,
                            int level,
                            Category category,
                            String id,
                            List<String> targets,
                            String targetType,
                            int priority,
                            Context context,
                            @Nullable
                            PropertyContainer attributeSource) implements Attribute {

	public static Attribute of(int value, String type) {
		return new SimpleAttribute(type, value);
	}

	public static Attribute of(float value, String type) {
		return new SimpleAttribute(type, value);
	}

	@Override
	public CalculationOrder getOrder() {
		return
				Objects.requireNonNullElse(this.order, Attribute.super.getOrder());
	}

	@Override
	public String getAttributeType() {
		return this.attribute;
	}

	@Override
	public Function<Float, Float> getCalculation() {
		if (operation == NumericOperation.ADDITION) {
			return (current) -> current + leveledValue();
		} else if (operation == NumericOperation.MULTIPLICATION) {
			return (current) -> current * leveledValue();
		} else if (operation == NumericOperation.FORCE) {
			return (current) -> leveledValue();
		}
		return (current) -> current;
	}

	@Override
	public Matchable getPredicate() {
		return condition;
	}

	@Override
	public NumericOperation getOperation() {
		return operation;
	}

	@Override
	public float getValue() {
		return value;
	}

	@Override
	public float leveledValue() {
		if (getOperation() == NumericOperation.MULTIPLICATION) {
			if (getValue() >= 1) {
				return ((getValue() - 1) * getLevel()) + 1;
			} else if (getValue() < 1) {
				return 1 - (1 - getValue()) * getLevel();
			}
		}
		return value * level;
	}

	@Override
	public Category getCategory() {
		return category;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public Context getContext() {
		return Objects.requireNonNullElse(context, Contexts.UNDEFINED);
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String type() {
		return getAttributeType();
	}


	@Override
	public int hashCode() {
		return Objects.hash(attribute, operation, value, condition, order, level, category, id, priority, context);
	}

	@Override
	public Optional<PropertyContainer> source() {
		return Optional.ofNullable(attributeSource());
	}

	@Override
	public ComputedAttribute compute() {
		return ComputedAttribute.of(leveledValue(), type());
	}

	@Override
	public String toString() {
		if (!id.equals(EMPTY_IDENTIFIER)) {
			return id;
		}
		return "BaseAttribute{" +
				"attribute=" + attribute +
				", operation=" + operation +
				", value=" + value +
				", condition=" + condition +
				", order=" + order +
				", level=" + level +
				", category=" + category +
				", id='" + id + '\'' +
				", priority=" + priority +
				'}';
	}

	public static class BaseAttributeCodec {
		private static final NumericOperation DEFAULT_OPERATION = NumericOperation.ADDITION;
		private static final Matchable DEFAULT_CONDITION = Matchable.DEFAULT_TRUE;
		private static final CalculationOrder DEFAULT_ORDER = CalculationOrder.BASE;
		private static final int DEFAULT_LEVEL = 1;
		private static final Category DEFAULT_CATEGORY = Category.UNDEFINED;
		private static final String DEFAULT_ID = EMPTY_IDENTIFIER;
		private static final List<String> DEFAULT_TARGETS = Collections.emptyList();
		private static final String DEFAULT_TARGET_TYPE = EMPTY_IDENTIFIER;
		private static final int DEFAULT_PRIORITY = 0;
		private static final Context DEFAULT_CONTEXT = Contexts.UNDEFINED;

		public static final Codec<BaseAttribute> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.fieldOf("type").forGetter(BaseAttribute::attribute),
						NumericOperation.CODEC.optionalFieldOf("operation", DEFAULT_OPERATION).forGetter(BaseAttribute::operation),
						Codec.FLOAT.fieldOf("value").forGetter(BaseAttribute::value),
						Matchable.CODEC.optionalFieldOf("condition", DEFAULT_CONDITION).forGetter(BaseAttribute::condition),
						CalculationOrder.CODEC.optionalFieldOf("order", DEFAULT_ORDER).forGetter(BaseAttribute::order),
						Codec.INT.optionalFieldOf("level", DEFAULT_LEVEL).forGetter(BaseAttribute::level),
						Category.CODEC.optionalFieldOf("category", DEFAULT_CATEGORY).forGetter(BaseAttribute::category),
						Codec.STRING.optionalFieldOf("id", DEFAULT_ID).forGetter(BaseAttribute::id),
						Codec.list(Codec.STRING).optionalFieldOf("targets", DEFAULT_TARGETS).forGetter(BaseAttribute::targets),
						Codec.STRING.optionalFieldOf("targetType", DEFAULT_TARGET_TYPE).forGetter(BaseAttribute::targetType),
						Codec.INT.optionalFieldOf("priority", DEFAULT_PRIORITY).forGetter(BaseAttribute::priority),
						Context.CODEC.optionalFieldOf("context", DEFAULT_CONTEXT).forGetter(BaseAttribute::context)
				).apply(instance, (type, operation, value, condition, order, level, category, id, targets, targetType, priority, context) ->
						new BaseAttribute(type, operation, value, condition, order, level, category, id, targets, targetType, priority, context, null)
				)
		);
	}
}
