package com.sigmundgranaas.forgero.core.property.attribute;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.context.Context;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.TargetTypes;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

/**
 * Builder for building attributes. You can create fresh ones, from POJO or from an existing attribute.
 * Use this to modify or create custom attributes.
 */
public class AttributeBuilder {
	private final String type;
	private Category category;
	private CalculationOrder order = CalculationOrder.BASE;
	private Predicate<Target> condition = Attribute.DEFAULT_CONDITION;
	private NumericOperation operation = NumericOperation.ADDITION;
	private float value = 1;
	private int level = 1;

	private List<String> targets = Collections.emptyList();

	private String targetType = EMPTY_IDENTIFIER;
	private String id = EMPTY_IDENTIFIER;
	private int priority = 0;

	private Context context;


	public AttributeBuilder(String type) {
		this.type = type;
		this.category = Category.UNDEFINED;
	}

	public AttributeBuilder(AttributeType type) {
		this.type = type.toString();
		this.category = Category.UNDEFINED;
	}

	public static Attribute createAttributeFromPojo(PropertyPojo.Attribute attributePOJO) {
		return createAttributeBuilder(attributePOJO).build();
	}

	public static AttributeBuilder createAttributeBuilder(PropertyPojo.Attribute attributePOJO) {
		AttributeBuilder builder = new AttributeBuilder(attributePOJO.type)
				.applyOrder(attributePOJO.order);

		if (attributePOJO.condition != null) {
			Predicate<Target> condition;
			builder.targetType = attributePOJO.condition.target.toString();
			builder.targets = attributePOJO.condition.tag;
			if (attributePOJO.condition.target == TargetTypes.TOOL_PART_TYPE) {
				condition = (target) -> {
					if (!target.getTypes().contains(TargetTypes.TOOL_PART_TYPE)) {
						return true;
					}
					return target.isApplicable(new HashSet<>(attributePOJO.condition.tag), TargetTypes.TOOL_PART_TYPE);
				};
			} else if (attributePOJO.condition.target == TargetTypes.TOOL_TYPE) {
				condition = (target) -> {
					if (!target.getTypes().contains(TargetTypes.TOOL_TYPE)) {
						return true;
					}
					return target.isApplicable(new HashSet<>(attributePOJO.condition.tag), TargetTypes.TOOL_TYPE);
				};
			} else if (attributePOJO.condition.target == TargetTypes.TYPE) {
				condition = (target) -> {
					if (!target.getTypes().contains(TargetTypes.TYPE)) {
						return true;
					}
					return target.isApplicable(new HashSet<>(attributePOJO.condition.tag), TargetTypes.TYPE);
				};

			} else {
				condition = (target) ->
						target.isApplicable(new HashSet<>(attributePOJO.condition.tag), attributePOJO.condition.target);
			}
			builder.applyCondition(condition);
		}
		builder.applyCategory(Objects.requireNonNullElse(attributePOJO.category, Category.UNDEFINED));

		builder.applyValue(attributePOJO.value);
		builder.applyOperation(attributePOJO.operation);

		builder.applyContext(Objects.requireNonNullElse(attributePOJO.context, Contexts.UNDEFINED));

		if (!attributePOJO.id.equals(EMPTY_IDENTIFIER)) {
			builder.applyId(attributePOJO.id);
		}
		if (attributePOJO.priority > 0) {
			builder.applyPriority(attributePOJO.priority);
		} else if (attributePOJO.condition != null) {
			builder.applyPriority(1);
		}

		return builder;
	}

	public static AttributeBuilder createAttributeBuilderFromAttribute(Attribute attribute) {
		AttributeBuilder builder = new AttributeBuilder(attribute.getAttributeType())
				.applyOrder(attribute.getOrder());
		builder.applyTargets(attribute.targets());
		builder.applyTargetType(attribute.targetType());
		builder.applyCondition(attribute.getCondition());
		builder.applyValue(attribute.getValue());
		builder.applyOperation(attribute.getOperation());
		builder.applyLevel(attribute.getLevel());
		builder.applyCategory(attribute.getCategory());
		builder.applyId(attribute.getId());
		builder.applyPriority(attribute.getPriority());
		builder.applyContext(attribute.getContext());
		return builder;
	}

	private void applyContext(Context context) {
		this.context = context;
	}

	public AttributeBuilder applyCondition(Predicate<Target> condition) {
		this.condition = condition;
		return this;
	}

	public AttributeBuilder applyOrder(CalculationOrder order) {
		this.order = order;
		return this;
	}

	public AttributeBuilder applyCategory(Category category) {
		this.category = category;
		return this;
	}

	public AttributeBuilder applyOperation(NumericOperation operation) {
		this.operation = operation;
		return this;
	}

	public AttributeBuilder applyValue(float value) {
		this.value = value;
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public AttributeBuilder applyLevel(int level) {
		this.level = level;
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public AttributeBuilder applyId(String id) {
		this.id = id;
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public AttributeBuilder applyPriority(int priority) {
		this.priority = priority;
		return this;
	}


	@SuppressWarnings("UnusedReturnValue")
	public AttributeBuilder applyTargets(List<String> targets) {
		this.targets = targets;
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public AttributeBuilder applyTargetType(String targetType) {
		this.targetType = targetType;
		return this;
	}


	public Attribute build() {
		return new BaseAttribute(type, operation, value, condition, order, level, category, id, targets, targetType, priority, context);
	}
}
