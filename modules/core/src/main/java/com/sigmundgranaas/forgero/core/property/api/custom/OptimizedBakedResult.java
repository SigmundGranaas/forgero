package com.sigmundgranaas.forgero.core.property.api.custom;


import java.util.List;
import java.util.stream.Stream;

/**
 * A generic, optimized intermediate baked result for conditional properties.
 * It pre-sorts properties into those that are fully static and those that require
 * dynamic evaluation, making the final resolution step very fast.
 * This class replaces the need for a custom BakedResult for each property type.
 *
 * @param staticProperties  Properties with no dynamic conditions.
 * @param dynamicProperties Properties that require dynamic condition checking.
 * @param <P>               The type of the conditional property.
 */
public record OptimizedBakedResult<P extends ConditionalProperty>(List<P> staticProperties, List<P> dynamicProperties) {
	/**
	 * Produces a stream of all statically-valid properties: the unconditional ones plus
	 * those carrying dynamic conditions. Dynamic conditions are NOT evaluated here — they
	 * are data for the game layer, which filters via its runtime evaluator at the call
	 * sites that hold actual game state.
	 *
	 * @return A stream containing all compiled properties.
	 */
	public Stream<P> all() {
		return Stream.concat(staticProperties.stream(), dynamicProperties.stream());
	}
}
