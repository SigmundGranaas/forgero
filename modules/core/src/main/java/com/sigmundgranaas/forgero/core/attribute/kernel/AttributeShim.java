package com.sigmundgranaas.forgero.core.attribute.kernel;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.DivisionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.SubtractionOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * THE compatibility boundary between the authored attribute format and the stat kernel.
 *
 * <p>This is deliberately the only place where legacy vocabulary (operator classes, authored
 * scope identifiers) is interpreted. The kernel itself never sees scope strings or operator
 * objects — it consumes {@link StatContribution}s. New concepts enter by extending the data
 * format and this mapping, never by teaching the fold about content.
 *
 * <p>Mappings:
 * <ul>
 *   <li>addition → ADD; subtraction → ADD(−v); multiplication → MULTIPLY; division → MULTIPLY(1/v)</li>
 *   <li>{@code scope/part-composite}, {@code scope/equipment-composite} → {@code gated}
 *       (offer/accept: placement and validity are structural, not labelled)</li>
 *   <li>{@code scope/local} → {@code local}</li>
 *   <li>{@code scope/upgrade} → plain (position decides: such contributions only reach a fold
 *       through an upgrade slot anyway; restrictive intent belongs in an in_slot_type condition)</li>
 *   <li>any other scope → warned once, treated as plain (e.g. stale forgero-1 contexts/*)</li>
 * </ul>
 */
public final class AttributeShim {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeShim.class);
	private static final Set<String> WARNED_SCOPES = ConcurrentHashMap.newKeySet();
	private static final Set<String> WARNED_OPERATORS = ConcurrentHashMap.newKeySet();

	private AttributeShim() {
	}

	/**
	 * @return The kernel contribution for an authored attribute, or empty if the attribute
	 * cannot be represented (unknown operator) — reported, never silently folded wrong.
	 */
	public static Optional<StatContribution> toContribution(Attribute attribute) {
		boolean gated = false;
		boolean local = false;
		boolean upgradeOnly = false;
		if (attribute.scope().isPresent()) {
			OpenIdentifier scope = attribute.scope().get();
			if (AttributeScope.isPartComposite(scope) || AttributeScope.isEquipmentComposite(scope)) {
				gated = true;
			} else if (AttributeScope.isLocal(scope)) {
				local = true;
			} else if (AttributeScope.isUpgrade(scope)) {
				upgradeOnly = true;
			} else {
				// Unrecognized scope (e.g. stale forgero-1 contexts/*): no handler matches it,
				// so it is INERT — it never contributed under the legacy engine and must not be
				// resurrected here. Reported once so the dead data can be removed.
				if (WARNED_SCOPES.add(scope.toString())) {
					LOGGER.warn("Inert attribute scope '{}' (attribute type {}): no handler; the "
							+ "contribution does not apply. Likely stale vocabulary — remove or migrate it.",
							scope, attribute.type());
				}
				return Optional.empty();
			}
		}

		Operator op = attribute.operator();
		if (op instanceof AdditionOperator) {
			return Optional.of(new StatContribution(attribute.type(), StatContribution.Operation.ADD,
					attribute.value(), gated, local, upgradeOnly, attribute.condition()));
		}
		if (op instanceof SubtractionOperator) {
			return Optional.of(new StatContribution(attribute.type(), StatContribution.Operation.ADD,
					-attribute.value(), gated, local, upgradeOnly, attribute.condition()));
		}
		if (op instanceof MultiplicationOperator) {
			return Optional.of(new StatContribution(attribute.type(), StatContribution.Operation.MULTIPLY,
					attribute.value(), gated, local, upgradeOnly, attribute.condition()));
		}
		if (op instanceof DivisionOperator) {
			float v = attribute.value();
			return Optional.of(new StatContribution(attribute.type(), StatContribution.Operation.MULTIPLY,
					v == 0f ? 0f : 1f / v, gated, local, upgradeOnly, attribute.condition()));
		}
		if (WARNED_OPERATORS.add(op.getClass().getSimpleName())) {
			LOGGER.warn("Attribute operator {} has no kernel mapping (attribute type {}); skipping. "
					+ "Model bounds as clamp fields, not fold operators.", op.getClass().getSimpleName(), attribute.type());
		}
		return Optional.empty();
	}
}
