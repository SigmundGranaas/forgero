package com.sigmundgranaas.forgero.core.attribute.composition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeContext;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles composition of attributes in the part-composite context.
 *
 * <p>This handler implements intersection logic for shape+material composition:</p>
 * <ul>
 *   <li>Groups attributes by type across all sources</li>
 *   <li>For each type, requires at least one base value AND one multiplier from DIFFERENT sources</li>
 *   <li>Computes the result: sum of bases × product of multipliers</li>
 *   <li>Attribute types missing either component are excluded from output</li>
 * </ul>
 *
 * <h3>Example:</h3>
 * <pre>
 * Sources:
 *   shape: {mining_speed: ×1.2, durability: ×1.0}
 *   material: {mining_speed: +6.0, durability: +240, armor: +5}
 *
 * Result:
 *   mining_speed: 6.0 × 1.2 = 7.2   (both have it)
 *   durability: 240 × 1.0 = 240     (both have it)
 *   armor: excluded                  (only material has it)
 * </pre>
 */
public class PartCompositeContextHandler implements AttributeContextHandler {

	public static final PartCompositeContextHandler INSTANCE = new PartCompositeContextHandler();

	@Override
	public OpenIdentifier contextId() {
		return AttributeContext.PART_COMPOSITE;
	}

	@Override
	public List<Attribute> compose(Map<String, List<Attribute>> sources) {
		if (sources.isEmpty()) {
			return List.of();
		}

		// Group all attributes by type, tracking which source they came from
		Map<OpenIdentifier, List<SourcedAttribute>> byType = new HashMap<>();

		for (var entry : sources.entrySet()) {
			String sourceName = entry.getKey();
			for (Attribute attr : entry.getValue()) {
				byType.computeIfAbsent(attr.type(), k -> new ArrayList<>())
						.add(new SourcedAttribute(sourceName, attr));
			}
		}

		// For each type, check if we can compose (need base + multiplier from different sources)
		List<Attribute> result = new ArrayList<>();

		for (var entry : byType.entrySet()) {
			OpenIdentifier type = entry.getKey();
			List<SourcedAttribute> attrs = entry.getValue();

			Optional<Float> composed = tryCompose(attrs);
			composed.ifPresent(value ->
					result.add(SimpleAttribute.resolved(type, value))
			);
		}

		return result;
	}

	/**
	 * Attempts to compose attributes of the same type from multiple sources.
	 *
	 * <p>Composition succeeds if there's at least one base (addition-like) and one multiplier
	 * from DIFFERENT sources. This ensures shapes define what's valid and materials provide values.</p>
	 *
	 * @param attrs Attributes of the same type from different sources
	 * @return The composed value, or empty if composition requirements aren't met
	 */
	private Optional<Float> tryCompose(List<SourcedAttribute> attrs) {
		// Separate into bases (addition, subtraction) and multipliers (multiplication, division)
		List<SourcedAttribute> bases = new ArrayList<>();
		List<SourcedAttribute> multipliers = new ArrayList<>();

		for (SourcedAttribute sa : attrs) {
			if (isBaseOperator(sa.attribute.operator())) {
				bases.add(sa);
			} else if (isMultiplierOperator(sa.attribute.operator())) {
				multipliers.add(sa);
			}
		}

		// Need at least one base AND one multiplier from DIFFERENT sources
		if (bases.isEmpty() || multipliers.isEmpty()) {
			return Optional.empty();
		}

		// Check that they come from different sources
		Set<String> baseSources = bases.stream().map(SourcedAttribute::source).collect(Collectors.toSet());
		Set<String> multiplierSources = multipliers.stream().map(SourcedAttribute::source).collect(Collectors.toSet());

		// At least one source must be different (e.g., base from material, multiplier from shape)
		boolean hasDifferentSources = !baseSources.equals(multiplierSources) ||
				baseSources.size() > 1 ||
				multiplierSources.size() > 1;

		if (!hasDifferentSources) {
			return Optional.empty();
		}

		// Compute: sum of bases × product of multipliers
		float baseSum = 0f;
		for (SourcedAttribute sa : bases) {
			baseSum = sa.attribute.operator().apply(baseSum, sa.attribute.value());
		}

		float multiplierProduct = 1f;
		for (SourcedAttribute sa : multipliers) {
			// For multiplication, we multiply the factors together
			multiplierProduct *= sa.attribute.value();
		}

		return Optional.of(baseSum * multiplierProduct);
	}

	private boolean isBaseOperator(Operator op) {
		// Addition and subtraction are "base" operators (order 1)
		return op.order() == 1;
	}

	private boolean isMultiplierOperator(Operator op) {
		// Multiplication and division are "multiplier" operators (order 2)
		return op.order() == 2;
	}

	/**
	 * An attribute paired with its source name.
	 */
	private record SourcedAttribute(String source, Attribute attribute) {
	}
}
