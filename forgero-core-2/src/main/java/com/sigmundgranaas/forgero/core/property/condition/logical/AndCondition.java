package com.sigmundgranaas.forgero.core.property.condition.logical;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.Collections;
import java.util.List;

/**
 * A container for AND logical operations. It can be instantiated as either a
 * purely static condition or a dynamic one, depending on its children.
 */
public class AndCondition {
	public static final OpenIdentifier TYPE = CodecConstants.IDENTIFIER_FACTORY.of("forgero:and");

	/**
	 * Creates a codec for the "predicates" block within an AND condition.
	 * It relies on the master ConditionCodec to parse its children recursively.
	 *
	 * @param conditionCodec The master codec for parsing nested conditions.
	 * @return A codec that parses a `Condition` object from a "predicates" field.
	 */
	public static Codec<Condition> codec(Codec<Condition> conditionCodec) {
		return conditionCodec.fieldOf("predicates").codec();
	}

	/**
	 * Factory method to create the appropriate condition type (static or dynamic)
	 * based on the contents of the child conditions.
	 *
	 * @param children The Condition object containing the child predicates.
	 * @return A StaticCondition if all children are static, otherwise a DynamicCondition.
	 */
	public static Object from(Condition children) {
		if (children.dynamicConditions().isEmpty()) {
			return new AndStatic(children.staticConditions());
		}
		return new AndDynamic(children.staticConditions(), children.dynamicConditions());
	}

	/**
	 * An AND condition that only contains static children and can be fully evaluated
	 * during the "bake" phase.
	 */
	public record AndStatic(List<StaticCondition> conditions) implements StaticCondition {
		@Override
		public boolean test(ResolutionContext context) {
			return conditions.stream().allMatch(c -> c.test(context));
		}

		@Override
		public OpenIdentifier type() {
			return TYPE;
		}

		public Condition toCondition() {
			return new Condition(conditions, Collections.emptyList());
		}
	}

	/**
	 * An AND condition that contains at least one dynamic child. It must be
	 * evaluated during the "apply" phase.
	 */
	public record AndDynamic(List<StaticCondition> staticConds, List<DynamicCondition> dynamicConds) implements DynamicCondition {
		@Override
		public boolean test(DynamicContext context) {
			return dynamicConds.stream().allMatch(c -> c.test(context));
		}

		@Override
		public OpenIdentifier type() {
			return TYPE;
		}

		public Condition toCondition() {
			return new Condition(staticConds, dynamicConds);
		}
	}
}
