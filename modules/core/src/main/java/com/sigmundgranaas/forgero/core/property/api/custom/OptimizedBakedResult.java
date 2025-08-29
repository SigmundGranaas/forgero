package com.sigmundgranaas.forgero.core.property.api.custom;

import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

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
	 * Produces a stream of all active properties for a given dynamic context.
	 * It efficiently filters only the dynamic properties and combines them with the static ones.
	 *
	 * @param context The dynamic context for the calculation.
	 * @return A stream containing all active properties.
	 */
	public Stream<P> stream(DynamicContext context) {
		Stream<P> activeDynamicStream = dynamicProperties.stream().filter(prop -> prop.test(context));
		return Stream.concat(staticProperties.stream(), activeDynamicStream);
	}
}
