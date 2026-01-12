package com.sigmundgranaas.forgero.core.component.api;

/**
 * Marker interface for components that contribute attributes for composition and inspection
 * but do NOT apply them when held.
 * <p>
 * Contributing components are the building blocks of equipment: parts and materials
 * that expose attributes for use during composition. When a player holds an item backed
 * by a ContributingComponent (e.g., a pickaxe_head), no attributes are applied to the player.
 * <p>
 * This distinction solves the problem of parts incorrectly applying attack damage when held.
 * Parts should be inspectable (you can see their stats) but not functional as weapons.
 * <p>
 * <b>Semantic distinction:</b>
 * <ul>
 *   <li>{@link EquipmentComponent} - Applies attributes (terminal)</li>
 *   <li>{@link ContributingComponent} - Provides attributes for composition only</li>
 * </ul>
 * <p>
 * <b>Implementation classes:</b>
 * <ul>
 *   <li>StaticComponent</li>
 *   <li>ExtensiblePart</li>
 *   <li>StructuredPart</li>
 *   <li>StructuredExtensiblePart</li>
 * </ul>
 *
 * @see EquipmentComponent
 * @since 0.15.0
 */
public interface ContributingComponent extends Component {

	/**
	 * Indicates whether this component applies its attributes when held or worn.
	 * <p>
	 * For contributing components, this always returns {@code false}. These components
	 * expose attributes for inspection and composition but do not apply them to players.
	 *
	 * @return {@code false} always, since contributing components don't apply attributes.
	 */
	default boolean appliesAttributes() {
		return false;
	}
}
