package com.sigmundgranaas.forgero.core.property.attribute;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningLevel;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.core.state.State;

import java.util.List;

public class AttributeHelper {
	private final State state;

	public AttributeHelper(State state) {
		this.state = state;
	}

	public static AttributeHelper of(State state) {
		return new AttributeHelper(state);
	}

	public float attribute(String type) {
		return switch (type) {
			case Rarity.KEY -> rarity();
			case MiningLevel.KEY -> miningLevel();
			case Durability.KEY -> durability();
			default -> 0f;
		};
	}

	public int rarity() {
		int rarity = (int) state.stream().applyAttribute(Rarity.KEY);
		return rarity;
	}

	public int miningLevel() {
		return (int) state.stream().applyAttribute(MiningLevel.KEY);
	}

	public int durability() {
		return Attribute.of(state, Durability.KEY).asInt();
	}

	public List<Property> attributes() {
		return state.getRootProperties();
	}
}
