package com.sigmundgranaas.forgero.core.state.composite;

import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.state.composite.ConstructedComposite.ConstructBuilder.builder;

public class ConstructedComposite extends BaseComposite implements ConstructedState {
    private final List<State> parts;

    protected ConstructedComposite(SlotContainer slotContainer, IdentifiableContainer id, List<State> parts) {
        super(slotContainer, id);
        this.parts = parts;
    }

    @Override
    public List<State> components() {
        return Stream.of(parts(), upgrades()).flatMap(List::stream).toList();
    }

    @Override
    public ConstructedComposite upgrade(State upgrade) {
        return toBuilder()
                .addUpgrade(upgrade)
                .build();
    }

    @Override
    public ConstructedComposite removeUpgrade(String id) {
        return this;
    }

    @Override
    public List<State> parts() {
        return parts;
    }

    public ConstructBuilder toBuilder() {
        return builder()
                .addIngredients(parts())
                .addUpgrades(slots())
                .type(type())
                .id(identifier());
    }

    @Override
    public boolean test(Matchable match, Context context) {
        if (match instanceof Type typeMatch) {
            if (this.type().test(typeMatch, context)) {
                return true;
            } else {
                return parts().stream().anyMatch(ingredient -> ingredient.test(match, context));
            }
        }
        if (match instanceof NameMatch name) {
            if (name.test(this, context)) {
                return true;
            } else {
                return parts().stream().anyMatch(ingredient -> ingredient.test(name, context));
            }
        }
        return false;
    }

    @Override
    public ConstructedComposite copy() {
        return toBuilder().build();
    }

    public static class ConstructBuilder extends BaseCompositeBuilder<ConstructBuilder> {
        public ConstructBuilder() {
            this.ingredientList = new ArrayList<>();
            this.upgradeContainer = SlotContainer.of(Collections.emptyList());
        }

        public static ConstructBuilder builder() {
            return new ConstructBuilder();
        }

        @Override
        public ConstructedComposite build() {
            compositeName();
            var id = new IdentifiableContainer(name, nameSpace, type);
            return new ConstructedComposite(upgradeContainer, id, ingredientList);
        }
    }
}
