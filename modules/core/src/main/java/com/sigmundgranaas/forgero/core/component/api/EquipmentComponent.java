package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.compiled.CompiledProperties;

import java.util.List;

/**
 * Interface for components that are "terminal" - they apply their attributes
 * directly to the player when held or worn.
 * <p>
 * Equipment components are the end-result of composition: tools, weapons, and armor
 * that players actually use. When a player holds an item backed by an EquipmentComponent,
 * its attributes (attack damage, attack speed, armor, etc.) are applied to the player.
 * <p>
 * <b>The compiled boundary:</b> a terminal component compiles its whole tree once, at
 * construction, into a {@link CompiledProperties} artifact — attributes plus all property
 * lists, with static conditions resolved away. The game layer reads this artifact and never
 * re-traverses the tree. See {@code docs/ADR-002-compiler-in-the-factory.md}.
 * <p>
 * <b>Semantic distinction:</b>
 * <ul>
 *   <li>{@link EquipmentComponent} - Terminal: holds the compiled artifact, applies attributes</li>
 *   <li>{@link ContributingComponent} - Provides attributes for composition only</li>
 * </ul>
 *
 * @see ContributingComponent
 * @since 0.15.0
 */
public interface EquipmentComponent extends Component {

	/**
	 * Returns the compiled artifact for this terminal component, produced at creation time.
	 *
	 * @return The compiled properties (attributes + compiled property lists)
	 */
	CompiledProperties compiled();

	/**
	 * Returns the pre-baked attributes for this equipment (a view of {@link #compiled()}).
	 *
	 * @return The baked attributes
	 */
	default BakedAttributes bakedAttributes() {
		return compiled().attributes();
	}

	/**
	 * Reads the compiled property list of the given type from this terminal's artifact.
	 *
	 * @param key The property type's resolution key.
	 * @return The compiled properties (dynamic conditions carried as data), or empty.
	 */
	default <P> List<P> properties(ResolutionKey<List<P>> key) {
		return compiled().get(key);
	}

	/**
	 * Gets the value of a specific attribute type.
	 *
	 * @param type    The attribute type (e.g., attack_damage, durability)
	 * @return The computed attribute value
	 */
	default float getAttribute(OpenIdentifier type) {
		return compiled().attributes().get(type).value();
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
