package com.sigmundgranaas.forgero.common.runtime;

import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.logical.AndCondition;
import com.sigmundgranaas.forgero.core.condition.logical.NotCondition;
import com.sigmundgranaas.forgero.core.condition.logical.OrCondition;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * The single game-side evaluator for dynamic conditions.
 *
 * <p>Core compiles properties with their dynamic conditions attached as data; this class
 * is where that data meets live game state. It is the ONLY place dynamic conditions are
 * evaluated — the component tree itself is a pure compile-time structure.
 *
 * <p>Evaluation semantics are ported verbatim from the former core-side implementations:
 * AND requires all children to pass, OR requires any, NOT inverts its child, and the
 * {@code Condition.any()} wrapper passes if any inner Condition's dynamic conditions all
 * pass (an inner Condition with no dynamic conditions counts as a pass, since its static
 * conditions already passed during compilation).
 */
public final class RuntimeConditions {
	private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeConditions.class);

	private RuntimeConditions() {
	}

	/**
	 * Tests all dynamic conditions of a property's condition against runtime context.
	 *
	 * @return true if the condition is null, has no dynamic conditions, or all of them pass.
	 */
	public static boolean test(@Nullable Condition condition, DynamicContext context) {
		if (condition == null) {
			return true;
		}
		return condition.dynamicConditions().stream().allMatch(c -> testCondition(c, context));
	}

	/**
	 * Tests a single dynamic condition against runtime context.
	 */
	public static boolean testCondition(DynamicCondition condition, DynamicContext context) {
		if (condition instanceof EvaluableCondition evaluable) {
			return evaluable.test(context);
		}
		if (condition instanceof Condition.OrDynamicCondition or) {
			for (Condition inner : or.conditions()) {
				if (inner.dynamicConditions().isEmpty()) {
					return true;
				}
				boolean allDynamicPass = inner.dynamicConditions().stream()
						.allMatch(dc -> testCondition(dc, context));
				if (allDynamicPass) {
					return true;
				}
			}
			return false;
		}
		if (condition instanceof AndCondition.AndDynamic and) {
			return and.dynamicConds().stream().allMatch(c -> testCondition(c, context));
		}
		if (condition instanceof OrCondition.OrDynamic or) {
			return or.dynamicConds().stream().anyMatch(c -> testCondition(c, context));
		}
		if (condition instanceof NotCondition.NotDynamic not) {
			boolean dynamicResult = not.dynamicCond() == null || testCondition(not.dynamicCond(), context);
			return !dynamicResult;
		}
		LOGGER.warn("Unknown dynamic condition type {} ({}); treating as failed.",
				condition.type(), condition.getClass().getName());
		return false;
	}

	/**
	 * Filters compiled conditional properties by their dynamic conditions.
	 * Replaces the former ConditionalProperty#test(DynamicContext) core-side helper.
	 */
	public static <P extends ConditionalProperty> List<P> filter(Collection<P> properties, DynamicContext context) {
		return properties.stream()
				.filter(p -> test(p.condition(), context))
				.toList();
	}
}
