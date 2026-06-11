package com.sigmundgranaas.forgero.core.condition.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;

/**
 * A condition evaluated during the static "bake" phase of property resolution.
 *
 * <p>Static conditions test properties against the <em>structural context</em> of a component:
 * its position in the component tree, its parent/sibling relationships, slot containment,
 * tags, and depth. These conditions are evaluated once during baking and their results
 * are cached, making them efficient for repeated queries.
 *
 * <h2>When Static Conditions Are Evaluated</h2>
 * <pre>
 * Component Tree
 *       ↓
 * ComponentTraversal.traverse()
 *       ↓
 * CompilerPass.bake() ◄── Static conditions evaluated HERE
 *       ↓
 * Cached intermediate result (e.g., BakedAttributes)
 *       ↓
 * CompilerPass.apply() ◄── Dynamic conditions evaluated here
 *       ↓
 * Final result
 * </pre>
 *
 * <h2>Available Context Information</h2>
 * The {@link ResolutionContext} provides access to:
 * <ul>
 *   <li>{@code self()} - The component whose property is being evaluated</li>
 *   <li>{@code root()} - The root of the resolution tree (e.g., the pickaxe)</li>
 *   <li>{@code isRoot()} - Whether self is the root component</li>
 *   <li>{@code getDepth()} - Tree depth (root=0, children=1, etc.)</li>
 *   <li>{@code getSlot()} - The mutable upgrade slot containing self (if any)</li>
 *   <li>{@code getPart()} - The immutable structure part containing self (if any)</li>
 *   <li>{@code getParent()} - The direct parent component</li>
 *   <li>{@code getSiblings()} - Other components sharing the same parent</li>
 *   <li>{@code findInRoot(slotType)} - Find a component in a specific slot anywhere in tree</li>
 * </ul>
 *
 * <h2>Built-in Implementations</h2>
 * <table>
 *   <tr><th>Condition</th><th>JSON Type</th><th>Purpose</th></tr>
 *   <tr><td>{@code IsRootCondition}</td><td>{@code forgero:is_root}</td><td>True if component is the root</td></tr>
 *   <tr><td>{@code InSlotTypeCondition}</td><td>{@code forgero:in_slot_type}</td><td>True if in a specific slot type</td></tr>
 *   <tr><td>{@code AtDepthCondition}</td><td>{@code forgero:at_depth}</td><td>True if at specific tree depth</td></tr>
 *   <tr><td>{@code TagMatchCondition}</td><td>{@code forgero:self_has_tag}</td><td>True if component has a tag</td></tr>
 *   <tr><td>{@code IdMatchCondition}</td><td>{@code forgero:id_match}</td><td>True if component ID matches</td></tr>
 *   <tr><td>{@code HasSiblingCondition}</td><td>{@code forgero:has_sibling}</td><td>True if has sibling with ID</td></tr>
 *   <tr><td>{@code SlotContainsCondition}</td><td>{@code forgero:slot_contains}</td><td>True if slot has component with tag</td></tr>
 *   <tr><td>{@code HasOtherContributorCondition}</td><td>{@code forgero:has_other_contributor}</td><td>True if other component has same attribute</td></tr>
 *   <tr><td>{@code AllTagsMatchCondition}</td><td>{@code forgero:self_has_all_tags}</td><td>True if component has ALL tags (AND)</td></tr>
 *   <tr><td>{@code AnyTagMatchCondition}</td><td>{@code forgero:self_has_any_tag}</td><td>True if component has ANY tag (OR)</td></tr>
 * </table>
 *
 * <h2>Example JSON Usage</h2>
 * <pre>{@code
 * {
 *   "attributes": [{
 *     "type": "forgero:attack_damage",
 *     "value": 5.0,
 *     "condition": {
 *       "static": [
 *         { "type": "forgero:in_slot_type", "slot_type": "forgero:head_slot" }
 *       ]
 *     }
 *   }]
 * }
 * }</pre>
 *
 * @see DynamicCondition for runtime game-state conditions
 * @see Condition for the unified condition container
 * @see ResolutionContext for available context information
 */
public interface StaticCondition {
	/**
	 * Tests this condition against the component's structural context.
	 *
	 * <p>This method is called once during the bake phase for each property
	 * with this condition. The result determines whether the property is
	 * included in the baked intermediate result.
	 *
	 * @param context The resolution context containing structural information
	 *                about the component being evaluated
	 * @return {@code true} if the condition passes and the property should be included,
	 *         {@code false} if the property should be filtered out
	 */
	boolean test(ResolutionContext context);

	/**
	 * Returns the type identifier for this condition.
	 *
	 * <p>This identifier is used for codec dispatch during JSON deserialization.
	 * It should match the "type" field in JSON condition definitions.
	 *
	 * @return The condition type identifier (e.g., "forgero:is_root", "forgero:in_slot_type")
	 */
	OpenIdentifier type();
}
