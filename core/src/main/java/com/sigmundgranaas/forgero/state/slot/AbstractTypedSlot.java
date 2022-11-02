package com.sigmundgranaas.forgero.state.slot;

import com.sigmundgranaas.forgero.property.attribute.Category;
import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Set;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

public abstract class AbstractTypedSlot implements Slot {
    private final Type type;
    private final int index;
    private final String description;
    protected final Set<Category> categories;

    public AbstractTypedSlot(int index, Type type, String description, Set<Category> categories) {
        this.index = index;
        this.type = type;
        this.description = description;
        this.categories = categories;
    }

    @Override
    public int index() {
        return index;
    }

    public Type type() {
        return type;
    }

    @Override
    public String description() {
        return description.equals(EMPTY_IDENTIFIER) ? type().typeName().toLowerCase() : description;
    }

    @Override
    public Set<Category> category() {
        return categories;
    }

    @Override
    public String identifier() {
        return description();
    }

    @Override
    public boolean test(Matchable match, Context context) {
        if (match instanceof State state) {
            return state.type().test(this.type, context);
        } else if (match instanceof Type type) {
            return this.type.test(type, context);
        }
        return false;
    }

}
