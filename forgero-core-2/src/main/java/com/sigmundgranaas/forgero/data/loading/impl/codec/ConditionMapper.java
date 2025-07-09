package com.sigmundgranaas.forgero.data.loading.api.data.loader;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticConditions;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A utility class to map condition DTOs from data files to runtime Condition objects.
 */
public class ConditionMapper {
	private final IdentifierFactory idFactory;

	public ConditionMapper(IdentifierFactory idFactory) {
		this.idFactory = idFactory;
	}

	public Condition apply(@Nullable ConditionData conditionData) {
		if (conditionData == null || conditionData.predicates().isEmpty()) {
			return Condition.ALWAYS_TRUE;
		}

		List<StaticCondition> staticConditions = new ArrayList<>();
		List<DynamicCondition> dynamicConditions = new ArrayList<>();

		for (PredicateData predicateData : conditionData.predicates()) {
			mapStaticPredicate(predicateData).ifPresent(staticConditions::add);
			mapDynamicPredicate(predicateData).ifPresent(dynamicConditions::add);
		}
		return new Condition(staticConditions, dynamicConditions);
	}

	private Optional<StaticCondition> mapStaticPredicate(PredicateData data) {
		if (data instanceof TagMatchPredicateData tagMatch) {
			if (tagMatch.type().path().equals("self_has_tag")) {
				return Optional.of(StaticConditions.selfHasTag(tagMatch.tag().path()));
			} else if (tagMatch.type().path().equals("root_has_tag")) {
				return Optional.of(StaticConditions.rootHasTag(tagMatch.tag().path()));
			}
		} else if (data instanceof InSlotTypePredicateData inSlotType) {
			return Optional.of(StaticConditions.selfInSlot(inSlotType.slotType()));
		} else if (data instanceof SlotContainsPredicateData slotContains) {
			return Optional.of(StaticConditions.slotContains(idFactory.of(slotContains.slot()), slotContains.tag().path()));
		} else if (data instanceof AndPredicateData andData) {
			List<StaticCondition> children = andData.predicates().stream()
					.flatMap(pred -> mapStaticPredicate(pred).stream())
					.toList();
			return Optional.of(ctx -> children.stream().allMatch(c -> c.test(ctx)));
		} else if (data instanceof OrPredicateData orData) {
			List<StaticCondition> children = orData.predicates().stream()
					.flatMap(pred -> mapStaticPredicate(pred).stream())
					.toList();
			return Optional.of(ctx -> children.stream().anyMatch(c -> c.test(ctx)));
		} else if (data instanceof NotPredicateData notData) {
			return mapStaticPredicate(notData.predicate())
					.map(pred -> (StaticCondition) ctx -> !pred.test(ctx));
		}
		return Optional.empty();
	}

	private Optional<DynamicCondition> mapDynamicPredicate(PredicateData data) {
		// Dynamic conditions can be added here in the future
		return Optional.empty();
	}
}
