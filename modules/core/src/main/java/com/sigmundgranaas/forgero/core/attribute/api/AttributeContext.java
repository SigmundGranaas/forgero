package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * Defines the context in which an attribute should be processed.
 *
 * <p>Context determines WHERE and HOW an attribute participates in composition,
 * NOT when it applies (that's what conditions are for).</p>
 *
 * <h3>Built-in Contexts:</h3>
 * <ul>
 *   <li>{@link #LOCAL} - Only applies to the component itself, does not propagate</li>
 *   <li>{@link #PART_COMPOSITE} - Participates in shape+material composition for parts</li>
 *   <li>{@link #EQUIPMENT_COMPOSITE} - Participates in parts→equipment composition</li>
 *   <li>{@link #UPGRADE} - Applied only when installed as an upgrade</li>
 * </ul>
 *
 * <p>An attribute with no context (empty Optional) is a "default" attribute that
 * propagates normally without special composition handling.</p>
 *
 * <h3>Example JSON:</h3>
 * <pre>
 * {
 *   "type": "forgero:mining_speed",
 *   "context": "forgero:part-composite",
 *   "computation": { "multiply": 1.2 }
 * }
 * </pre>
 */
public final class AttributeContext {

	/**
	 * Attribute only applies to the component it's defined on.
	 *
	 * <p>Local attributes do NOT propagate to parent components during composition.
	 * Use this for attributes that should only affect the immediate component,
	 * like internal modifiers or component-specific bonuses.</p>
	 *
	 * <p>Example: A material's "crafting bonus" that only matters when
	 * the material itself is being processed, not when it's part of a tool.</p>
	 */
	public static final OpenIdentifier LOCAL = new OpenIdentifier("forgero", "local");

	/**
	 * Participates in shape+material composition when building parts.
	 *
	 * <p>Attributes with this context from shape and material are composed together
	 * using intersection logic: only attribute types present in BOTH sources
	 * (with at least one base and one multiplier) produce output.</p>
	 */
	public static final OpenIdentifier PART_COMPOSITE = new OpenIdentifier("forgero", "part-composite");

	/**
	 * Participates in parts→equipment composition.
	 *
	 * <p>Used when combining multiple parts (head, handle, etc.) into equipment.</p>
	 */
	public static final OpenIdentifier EQUIPMENT_COMPOSITE = new OpenIdentifier("forgero", "equipment-composite");

	/**
	 * Applied only when the component is installed as an upgrade.
	 *
	 * <p>Upgrade context attributes are filtered out unless the component
	 * is placed in an upgrade slot. This prevents upgrade bonuses from
	 * applying when the component is used as a primary material.</p>
	 *
	 * <p>Example: A gem's "socket bonus" that only applies when the gem
	 * is installed in a tool's upgrade slot, not when used as crafting material.</p>
	 */
	public static final OpenIdentifier UPGRADE = new OpenIdentifier("forgero", "upgrade");

	private AttributeContext() {
		// Utility class
	}

	/**
	 * Checks if the given context is a composite context (requires composition handling).
	 */
	public static boolean isCompositeContext(OpenIdentifier context) {
		return PART_COMPOSITE.equals(context) || EQUIPMENT_COMPOSITE.equals(context);
	}

	/**
	 * Checks if the given context is a filter context (simple include/exclude).
	 */
	public static boolean isFilterContext(OpenIdentifier context) {
		return LOCAL.equals(context) || UPGRADE.equals(context);
	}

	/**
	 * Checks if an attribute's context matches a slot's context for upgrade filtering.
	 * Uses simple equality matching without tag hierarchy resolution.
	 *
	 * <p>Matching rules:</p>
	 * <ul>
	 *   <li>Attribute with no context → matches any slot (default behavior)</li>
	 *   <li>Attribute with {@link #UPGRADE} context → matches any upgrade slot</li>
	 *   <li>Attribute with specific context → matches only if contexts are equal</li>
	 * </ul>
	 *
	 * @param attributeContext The attribute's context (may be empty for default attributes)
	 * @param slotContext The slot's context (may be empty for unfiltered slots)
	 * @return true if the attribute should be included for this slot
	 */
	public static boolean matchesSlotContext(java.util.Optional<OpenIdentifier> attributeContext, java.util.Optional<OpenIdentifier> slotContext) {
		if (attributeContext.isEmpty()) {
			return true;
		}

		OpenIdentifier attrCtx = attributeContext.get();

		if (UPGRADE.equals(attrCtx)) {
			return true;
		}

		if (slotContext.isEmpty()) {
			return true;
		}

		return attrCtx.equals(slotContext.get());
	}

	/**
	 * Checks if an attribute's context matches a slot's context using tag hierarchy resolution.
	 *
	 * <p>Matching rules:</p>
	 * <ul>
	 *   <li>Attribute with no context → matches any slot (default behavior)</li>
	 *   <li>Attribute with {@link #UPGRADE} context → matches any upgrade slot</li>
	 *   <li>Attribute context matches if equal to slot context OR is a descendant of slot context</li>
	 * </ul>
	 *
	 * <p>Example: Slot with context "forgero:offensive" matches attributes with context
	 * "forgero:offensive" or any child context like "forgero:melee_offensive".</p>
	 *
	 * @param attributeContext The attribute's context (may be empty for default attributes)
	 * @param slotContext The slot's context (may be empty for unfiltered slots)
	 * @param tagResolver The tag resolver for checking tag hierarchy relationships
	 * @return true if the attribute should be included for this slot
	 */
	public static boolean matchesSlotContext(
			java.util.Optional<OpenIdentifier> attributeContext,
			java.util.Optional<OpenIdentifier> slotContext,
			com.sigmundgranaas.forgero.common.tags.api.TagResolver tagResolver
	) {
		if (attributeContext.isEmpty()) {
			return true;
		}

		OpenIdentifier attrCtx = attributeContext.get();

		if (UPGRADE.equals(attrCtx)) {
			return true;
		}

		if (slotContext.isEmpty()) {
			return true;
		}

		OpenIdentifier slotCtx = slotContext.get();

		if (attrCtx.equals(slotCtx)) {
			return true;
		}

		return tagResolver.getDescendants(slotCtx).contains(attrCtx);
	}
}
