package com.sigmundgranaas.forgero.state.slot;

import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

public class EmptySlot extends AbstractTypedSlot {
    public EmptySlot(int index, Type type, String description) {
        super(index, type, description);
    }

    public static List<? extends Slot> of(List<Type> types) {
        return IntStream.range(0, types.size()).mapToObj((arrayIndex) -> new EmptySlot(arrayIndex, types.get(arrayIndex), EMPTY_IDENTIFIER)).toList();
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
    public Optional<Slot> fill(State slottable) {
        if (test(slottable, Context.of())) {
            return Optional.of(new FilledSlot(index(), type(), slottable, description()));
        }
        return Optional.empty();
    }

    @Override
    public boolean test(Matchable match, Context context) {
        return super.test(match, context);
    }
}
