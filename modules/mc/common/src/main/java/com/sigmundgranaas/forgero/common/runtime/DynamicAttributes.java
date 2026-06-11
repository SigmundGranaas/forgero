package com.sigmundgranaas.forgero.common.runtime;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Applies an item's dynamic-conditional attributes at the game layer.
 *
 * <p>This is the runtime half of the dynamic-attribute story. Core compiles an item's
 * attributes at construction: unconditional values are folded into the stat
 * ({@code PrecomputedAttribute.value()}), while attributes carrying dynamic conditions are
 * carried through as DATA in {@code PrecomputedAttribute.conditionalAttributes()} — never
 * evaluated by core, because their truth depends on live game state.
 *
 * <p>This class is where that data meets the world. At a context-bearing event (e.g. an
 * attack, where the attacker and target exist), the game layer builds a {@link DynamicContext}
 * and asks for the qualifying bonus: each conditional attribute whose dynamic conditions pass
 * (via {@link RuntimeConditions}) contributes through its operator. Runtime state never enters
 * the component tree — it only reads the already-compiled artifact. See
 * {@code docs/ADR-002-compiler-in-the-factory.md}.
 *
 * <p>Example: a dagger declares {@code +4 forgero:attack_damage} gated on
 * {@code is_sneaking}. The base attack damage (vanilla attribute modifier) is unchanged; the
 * +4 is applied here, at the attack event, only when the attacker is sneaking.
 */
public final class DynamicAttributes {

	private DynamicAttributes() {
	}

	/**
	 * Computes the dynamic bonus for an attribute type on a stack, given runtime context.
	 *
	 * @param stack   The item whose compiled conditional attributes are read.
	 * @param type    The attribute type (e.g. {@code DefaultAttributes.ATTACK_DAMAGE}).
	 * @param context The live runtime context (source/target entities, world, ...).
	 * @return The bonus to add to the base value: the operator-fold (from 0) of every
	 * conditional attribute of this type whose dynamic conditions pass. 0 for non-Forgero or
	 * non-terminal items, or when no conditional attribute qualifies.
	 */
	public static float bonus(ItemStack stack, OpenIdentifier type, DynamicContext context) {
		if (stack == null || stack.isEmpty() || !ForgeroApi.isInitialized()) {
			return 0f;
		}
		return ForgeroApi.converter().toComponent(stack)
				.filter(component -> component instanceof EquipmentComponent)
				.map(component -> ((EquipmentComponent) component).compiled().attributes().get(type).conditionalAttributes())
				.map(conditionals -> apply(conditionals, context))
				.orElse(0f);
	}

	/**
	 * The pure fold: sums (via each attribute's operator, from 0) the conditional attributes
	 * whose dynamic conditions pass against the context. Exposed for direct testing.
	 */
	public static float apply(List<Attribute> conditionals, DynamicContext context) {
		float value = 0f;
		for (Attribute attribute : conditionals) {
			if (RuntimeConditions.test(attribute.condition().orElse(null), context)) {
				value = attribute.operator().apply(value, attribute.value());
			}
		}
		return value;
	}
}
