package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.DivisionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.SubtractionOperator;
import com.sigmundgranaas.forgero.core.condition.api.Condition;

import java.util.Optional;

/**
 * Fluent builder for creating attributes with clear, self-documenting code.
 *
 * <h2>Usage Examples</h2>
 *
 * <pre>
 * // Material base value for part composition
 * Attribute ironDurability = Attribute.of(DURABILITY, 240)
 *     .forPartComposition()
 *     .build();
 *
 * // Shape multiplier for part composition
 * Attribute pickaxeMultiplier = Attribute.of(DURABILITY, 1.0f)
 *     .multiply()
 *     .forPartComposition()
 *     .build();
 *
 * // Upgrade-only bonus that applies when sneaking
 * Attribute sneakBonus = Attribute.of(ATTACK_DAMAGE, 5)
 *     .forUpgradeSlots()
 *     .when(Condition.isSneaking())
 *     .build();
 *
 * // Local attribute that doesn't propagate
 * Attribute internalValue = Attribute.of(INTERNAL_MODIFIER, 1.5f)
 *     .localOnly()
 *     .build();
 * </pre>
 *
 * @see Attribute#of(OpenIdentifier, float) Entry point for fluent building
 */
public final class AttributeBuilder {

	private final OpenIdentifier type;
	private final float value;
	private Optional<OpenIdentifier> id = Optional.empty();
	private Operator operator = AdditionOperator.getInstance();
	private int group = 0;
	private Optional<OpenIdentifier> scope = Optional.empty();
	private Optional<Condition> condition = Optional.empty();

	/**
	 * Creates a new builder for an attribute of the given type and value.
	 *
	 * @param type The attribute type (e.g., forgero:durability)
	 * @param value The numerical value
	 */
	public AttributeBuilder(OpenIdentifier type, float value) {
		this.type = type;
		this.value = value;
	}

	// ========================================================================
	// SCOPE METHODS (WHERE)
	// ========================================================================

	/**
	 * This attribute participates in shape+material part composition.
	 *
	 * <p>Use this for material base values and shape multipliers.</p>
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder forPartComposition() {
		this.scope = Optional.of(AttributeScope.PART_COMPOSITE);
		return this;
	}

	/**
	 * This attribute participates in parts→equipment composition.
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder forEquipmentComposition() {
		this.scope = Optional.of(AttributeScope.EQUIPMENT_COMPOSITE);
		return this;
	}

	/**
	 * This attribute only applies when installed in an upgrade slot.
	 *
	 * <p>Use this for gem bonuses, reinforcement effects, etc.</p>
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder forUpgradeSlots() {
		this.scope = Optional.of(AttributeScope.UPGRADE);
		return this;
	}

	/**
	 * This attribute only applies to this component, doesn't propagate.
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder localOnly() {
		this.scope = Optional.of(AttributeScope.LOCAL);
		return this;
	}

	/**
	 * This attribute has custom scope behavior.
	 *
	 * @param customScope The custom scope identifier
	 * @return this builder for chaining
	 */
	public AttributeBuilder withScope(OpenIdentifier customScope) {
		this.scope = Optional.ofNullable(customScope);
		return this;
	}

	/**
	 * Clears any previously set scope, making this a default attribute.
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder withoutScope() {
		this.scope = Optional.empty();
		return this;
	}

	// ========================================================================
	// OPERATOR METHODS (HOW)
	// ========================================================================

	/**
	 * This value adds to the total. (Default behavior)
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder add() {
		this.operator = AdditionOperator.getInstance();
		return this;
	}

	/**
	 * This value multiplies the total.
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder multiply() {
		this.operator = MultiplicationOperator.getInstance();
		return this;
	}

	/**
	 * This value subtracts from the total.
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder subtract() {
		this.operator = SubtractionOperator.getInstance();
		return this;
	}

	/**
	 * This value divides the total.
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder divide() {
		this.operator = DivisionOperator.getInstance();
		return this;
	}

	/**
	 * Use a custom operator.
	 *
	 * @param op The operator to use
	 * @return this builder for chaining
	 */
	public AttributeBuilder withOperator(Operator op) {
		this.operator = op;
		return this;
	}

	// ========================================================================
	// CONDITION METHODS (WHEN)
	// ========================================================================

	/**
	 * This attribute only applies when the condition is met.
	 *
	 * @param condition Runtime condition (static or dynamic)
	 * @return this builder for chaining
	 */
	public AttributeBuilder when(Condition condition) {
		this.condition = Optional.ofNullable(condition);
		return this;
	}

	/**
	 * This attribute only applies when ALL conditions are met.
	 *
	 * @param conditions The conditions that must all be true
	 * @return this builder for chaining
	 */
	public AttributeBuilder whenAll(Condition... conditions) {
		this.condition = Optional.of(Condition.all(conditions));
		return this;
	}

	/**
	 * This attribute only applies when ANY condition is met.
	 *
	 * @param conditions The conditions where at least one must be true
	 * @return this builder for chaining
	 */
	public AttributeBuilder whenAny(Condition... conditions) {
		this.condition = Optional.of(Condition.any(conditions));
		return this;
	}

	/**
	 * Clears any previously set condition, making this attribute always apply.
	 *
	 * @return this builder for chaining
	 */
	public AttributeBuilder always() {
		this.condition = Optional.empty();
		return this;
	}

	// ========================================================================
	// OTHER CONFIGURATION
	// ========================================================================

	/**
	 * Sets the computation group (lower = computed first).
	 *
	 * @param group The group number (0 is default, computed first)
	 * @return this builder for chaining
	 */
	public AttributeBuilder inGroup(int group) {
		this.group = group;
		return this;
	}

	/**
	 * Sets an optional identifier for this attribute instance.
	 *
	 * @param id The attribute's unique identifier
	 * @return this builder for chaining
	 */
	public AttributeBuilder withId(OpenIdentifier id) {
		this.id = Optional.ofNullable(id);
		return this;
	}

	/**
	 * Sets an optional identifier for this attribute instance using a string.
	 *
	 * @param id The attribute's unique identifier as "namespace:path"
	 * @return this builder for chaining
	 */
	public AttributeBuilder withId(String id) {
		this.id = Optional.ofNullable(id).map(OpenIdentifier::of);
		return this;
	}

	// ========================================================================
	// BUILD
	// ========================================================================

	/**
	 * Creates the attribute.
	 *
	 * @return A new immutable attribute instance
	 */
	public Attribute build() {
		return new SimpleAttribute(id, type, value, operator, group, scope, condition);
	}
}
