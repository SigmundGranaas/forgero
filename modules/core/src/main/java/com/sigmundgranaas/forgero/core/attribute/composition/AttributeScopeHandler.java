package com.sigmundgranaas.forgero.core.attribute.composition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;

import java.util.List;
import java.util.Map;

/**
 * Handles composition of attributes within a specific scope.
 *
 * <p>Each scope type (part-composite, equipment-composite, upgrade, etc.) has its own
 * handler that knows how to combine attributes from multiple sources.</p>
 *
 * <p>The handler is invoked by the component that understands the composition—for example,
 * StructuredPart invokes the part-composite handler because it knows about shape+material slots.</p>
 *
 * <h3>Example Implementation:</h3>
 * <pre>
 * public class PartCompositeHandler implements AttributeScopeHandler {
 *     {@literal @}Override
 *     public List&lt;Attribute&gt; compose(Map&lt;String, List&lt;Attribute&gt;&gt; sources) {
 *         // Intersection logic: only types with base+multiplier from different sources
 *     }
 * }
 * </pre>
 */
public interface AttributeScopeHandler {

	/**
	 * The scope identifier this handler processes.
	 *
	 * @return The scope identifier (e.g., "forgero:scope/part-composite")
	 */
	OpenIdentifier scopeId();

	/**
	 * Composes attributes from multiple named sources.
	 *
	 * <p>The sources map contains attributes grouped by their origin. For part composition,
	 * this might be {"shape": [...], "material": [...]}.</p>
	 *
	 * <p>The returned attributes are "resolved"—they no longer have a scope and are ready
	 * for the next stage of processing or final resolution.</p>
	 *
	 * @param sources Named sources of attributes to compose
	 * @return Composed attributes (typically with no scope, ready for propagation)
	 */
	List<Attribute> compose(Map<String, List<Attribute>> sources);
}
