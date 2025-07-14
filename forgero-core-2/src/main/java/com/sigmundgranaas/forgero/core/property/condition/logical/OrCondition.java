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
 * A container for OR logical operations. It can be instantiated as either a
 * purely static condition or a dynamic one, depending on its children.
 */
public class OrCondition {
	public static final OpenIdentifier TYPE = CodecConstants.IDENTIFIER_FACTORY.of("forgero:or");

	public static Codec<Condition> codec(Codec<Condition> conditionCodec) {
		return conditionCodec.fieldOf("predicates").codec();
	}

	public static Object from(Condition children) {
		if (children.dynamicConditions().isEmpty()) {
			return new OrStatic(children.staticConditions());
		}
		return new OrDynamic(children.staticConditions(), children.dynamicConditions());
	}

	public record OrStatic(List<StaticCondition> conditions) implements StaticCondition {
		@Override
		public boolean test(ResolutionContext context) {
			return conditions.stream().anyMatch(c -> c.test(context));
		}

		@Override
		public OpenIdentifier type() {
			return TYPE;
		}

		public Condition toCondition() {
			return new Condition(conditions, Collections.emptyList());
		}
	}

	public record OrDynamic(List<StaticCondition> staticConds, List<DynamicCondition> dynamicConds) implements DynamicCondition {
		@Override
		public boolean test(DynamicContext context) {
			return dynamicConds.stream().anyMatch(c -> c.test(context));
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
