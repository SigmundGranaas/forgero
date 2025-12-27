package com.sigmundgranaas.forgero.core.condition.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * A static condition that returns true if any OTHER component in the assembly
 * contributes an attribute of the specified type. This preserves the legacy
 * "2+ sources" composite requirement where attributes only combine when multiple
 * components contribute to the same attribute type.
 *
 * <p>Example JSON usage:</p>
 * <pre>
 * {
 *   "condition": {
 *     "static": [
 *       { "type": "forgero:has_other_contributor", "attribute_type": "forgero:durability" }
 *     ]
 *   }
 * }
 * </pre>
 *
 * <p>This condition traverses the entire component tree from the root, checking if
 * any component other than the current one (self) has an attribute matching the
 * specified type.</p>
 */
public record HasOtherContributorCondition(
		OpenIdentifier type,
		OpenIdentifier attributeType
) implements StaticCondition {

	public static final Codec<HasOtherContributorCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(HasOtherContributorCondition::type),
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("attribute_type").forGetter(HasOtherContributorCondition::attributeType)
			).apply(instance, HasOtherContributorCondition::new));

	@Override
	public boolean test(ResolutionContext context) {
		return hasOtherContributor(context.root(), context.self(), attributeType);
	}

	/**
	 * Recursively searches the component tree for any component (other than self)
	 * that has an attribute of the specified type.
	 *
	 * @param current  The current component being checked
	 * @param self     The component whose condition is being evaluated (excluded from search)
	 * @param attrType The attribute type to search for
	 * @return true if another component has an attribute of the specified type
	 */
	private boolean hasOtherContributor(Component current, Component self, OpenIdentifier attrType) {
		// Check if this component (other than self) has a matching attribute
		if (current != self) {
			boolean hasMatchingAttribute = current.properties(Attribute.KEY).stream()
					.anyMatch(attr -> attr.type().equals(attrType));
			if (hasMatchingAttribute) {
				return true;
			}
		}

		// Recurse into structured component slots
		if (current instanceof StructuredComponent structured) {
			for (var slot : structured.structure().slots().all()) {
				if (hasOtherContributor(slot.content(), self, attrType)) {
					return true;
				}
			}
		}

		// Recurse into customizable component upgrades
		if (current instanceof CustomizableComponent customizable) {
			for (var slot : customizable.upgrades().slots().all()) {
				if (slot.content().isPresent()) {
					if (hasOtherContributor(slot.content().get(), self, attrType)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
