package com.sigmundgranaas.forgero.core.state.upgrade.slot;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class EmptySlot extends AbstractTypedSlot {
	public EmptySlot(int index, Type type, String description, Set<Category> categories) {
		super(index, type, description, categories);
	}

	public static List<? extends Slot> of(List<Type> types, Set<Category> categories) {
		return IntStream.range(0, types.size()).mapToObj((arrayIndex) -> new EmptySlot(arrayIndex, types.get(arrayIndex), EMPTY_IDENTIFIER, categories)).toList();
	}

	@Override
	public boolean filled() {
		return false;
	}

	@Override
	public Optional<State> get() {
		return Optional.empty();
	}

	@Override
	public Slot empty() {
		return this;
	}

	@Override
	public Optional<Slot> fill(State slottable, Set<Category> categories) {
		assert categories != null;
		if (test(slottable, MatchContext.of())) {
			return Optional.of(new FilledSlot(index(), type(), slottable, description(), categories));
		}
		return Optional.empty();
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return super.test(match, context);
	}


	@Override
	public Slot copy() {
		return new EmptySlot(index, type, description, categories);
	}

	@Override
	public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return getRootProperties();
	}
}
