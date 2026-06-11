package com.sigmundgranaas.forgero.core.condition.logical;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.Collections;

/**
 * A container for NOT logical operations. It inverts the result of its single child.
 */
public class NotCondition {
	public static final OpenIdentifier TYPE = CodecConstants.IDENTIFIER_FACTORY.of("forgero:not");

	public static Codec<Condition> codec(Codec<Condition> conditionCodec) {
		return conditionCodec.fieldOf("predicate").codec();
	}

	public static LogicalConditionResult from(Condition child) {
		if (child.dynamicConditions().isEmpty()) {
			return new NotStatic(child.staticConditions().get(0));
		}
		return new NotDynamic(child.staticConditions().isEmpty() ? null : child.staticConditions().get(0),
				child.dynamicConditions().isEmpty() ? null : child.dynamicConditions().get(0));
	}

	public record NotStatic(StaticCondition condition) implements StaticCondition, LogicalConditionResult {
		@Override
		public boolean test(ResolutionContext context) {
			return !condition.test(context);
		}

		@Override
		public OpenIdentifier type() {
			return TYPE;
		}

		public Condition toCondition() {
			return new Condition(Collections.singletonList(condition), Collections.emptyList());
		}
	}

	public record NotDynamic(StaticCondition staticCond, DynamicCondition dynamicCond) implements DynamicCondition, LogicalConditionResult {
		@Override
		public OpenIdentifier type() {
			return TYPE;
		}

		public Condition toCondition() {
			return new Condition(
					staticCond == null ? Collections.emptyList() : Collections.singletonList(staticCond),
					dynamicCond == null ? Collections.emptyList() : Collections.singletonList(dynamicCond)
			);
		}
	}
}
