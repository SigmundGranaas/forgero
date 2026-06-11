package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;

/**
 * Interface for components that are "terminal" - they apply their attributes
 * directly to the player when held or worn.
 * <p>
 * Equipment components are the end-result of composition: tools, weapons, and armor
 * that players actually use. When a player holds an item backed by an EquipmentComponent,
 * its attributes (attack damage, attack speed, armor, etc.) are applied to the player.
 * <p>
 * <b>Attribute resolution:</b> Equipment components bake their attributes at creation
 * time for O(1) lookup. Use {@link #getAttribute(OpenIdentifier)} to
 * query attribute values.
 * <p>
 * <b>Semantic distinction:</b>
 * <ul>
 *   <li>{@link EquipmentComponent} - Applies attributes (terminal, pre-baked)</li>
 *   <li>{@link ContributingComponent} - Provides attributes for composition only</li>
 * </ul>
 *
 * @see ContributingComponent
 * @since 0.15.0
 */
public interface EquipmentComponent extends Component {

	/**
	 * Returns the pre-baked attributes for this equipment.
	 * <p>
	 * Attributes are baked at creation time, providing O(1) lookup by type.
	 *
	 * @return The baked attributes
	 */
	BakedAttributes bakedAttributes();

	/**
	 * Gets the value of a specific attribute type.
	 *
	 * @param type    The attribute type (e.g., attack_damage, durability)
	 * @return The computed attribute value
	 */
	default float getAttribute(OpenIdentifier type) {
		return bakedAttributes().get(type).value();
	}

	/**
	 * Indicates whether this component applies its attributes when held or worn.
	 *
	 * @return {@code true} always, since equipment components apply their attributes.
	 */
	default boolean appliesAttributes() {
		return true;
	}
}
