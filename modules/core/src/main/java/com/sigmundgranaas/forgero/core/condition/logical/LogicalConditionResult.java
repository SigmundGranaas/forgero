package com.sigmundgranaas.forgero.core.condition.logical;

/**
 * A sealed interface representing the result of parsing a logical condition.
 * This provides type safety for the factory methods in logical condition classes.
 */
public sealed interface LogicalConditionResult
		permits AndCondition.AndStatic, AndCondition.AndDynamic,
		OrCondition.OrStatic, OrCondition.OrDynamic,
		NotCondition.NotStatic, NotCondition.NotDynamic {
}
