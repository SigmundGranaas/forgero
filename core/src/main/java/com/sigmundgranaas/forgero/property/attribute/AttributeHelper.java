package com.sigmundgranaas.forgero.property.attribute;

import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;

import java.util.List;

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
        if (state instanceof Composite composite) {
            return rarity / composite.getCompositeCount();
        }
        return rarity;
    }

    public int miningLevel() {
        return (int) state.stream().applyAttribute(AttributeType.MINING_LEVEL);
    }

    public int durability() {
        return (int) state.stream().applyAttribute(AttributeType.DURABILITY);
    }

    public List<Property> attributes() {
        return state.getProperties();
    }
}
