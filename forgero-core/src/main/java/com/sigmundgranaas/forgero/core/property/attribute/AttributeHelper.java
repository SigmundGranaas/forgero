package com.sigmundgranaas.forgero.core.property.attribute;

import java.util.List;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.state.State;

public class AttributeHelper {
	private final State state;

	public AttributeHelper(State state) {
		this.state = state;
	}

	public static AttributeHelper of(State state) {
		return new AttributeHelper(state);
	}

	public float attribute(AttributeType type) {
		return switch (type) {
			case RARITY -> rarity();
			case MINING_LEVEL -> miningLevel();
			case DURABILITY -> durability();
			default -> 0f;
		};
	}

	public int rarity() {
		int rarity = (int) state.stream().applyAttribute(AttributeType.RARITY);
		return rarity;
	}

	public int miningLevel() {
		return (int) state.stream().applyAttribute(AttributeType.MINING_LEVEL);
	}

	public int durability() {
		return Attribute.of(state, Durability.KEY).asInt();
	}

	public List<Property> attributes() {
		return state.applyProperty(Target.EMPTY);
	}
}
