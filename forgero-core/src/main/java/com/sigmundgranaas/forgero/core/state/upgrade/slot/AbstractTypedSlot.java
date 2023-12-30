package com.sigmundgranaas.forgero.core.state.upgrade.slot;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.Locale;
import java.util.Set;

import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public abstract class AbstractTypedSlot implements Slot {
	protected final Set<Category> categories;
	protected final Type type;
	protected final int index;
	protected final String description;

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
		return description.equals(EMPTY_IDENTIFIER) ? type().typeName().toLowerCase(Locale.ENGLISH) : description;
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
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof State state) {
			return this.type.test(state.type(), context);
		} else if (match instanceof Type type) {
			return this.type.test(type, context);
		}
		return false;
	}

	@Override
	public String typeName() {
		return type.typeName();
	}


}
