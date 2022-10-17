package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CompositeModelEntry implements ModelMatcher {
    @Override
    public boolean match(Matchable state, Context context) {
        if (state instanceof Composite) {
            return true;
        }
        return false;
    }

    @Override
    public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, Context context) {
        if (state instanceof Composite composite) {
            var compositeModelTemplate = new CompositeModelTemplate();
            var compositeContext = context.add(composite.type());
            composite.ingredients().stream()
                    .map(stateEntry ->
                            provider.find(stateEntry).filter(matcher -> matcher.match(stateEntry, compositeContext)).flatMap(matcher -> matcher.get(stateEntry, provider, compositeContext))
                    ).flatMap(Optional::stream)
                    .forEach(compositeModelTemplate::add);
            composite.slots().stream()
                    .map(slot -> slot.get().flatMap(upgrade -> provider.find(upgrade).filter(matcher -> matcher.match(upgrade, compositeContext)).flatMap(matcher -> matcher.get(upgrade, provider, compositeContext)))
                    ).flatMap(Optional::stream)
                    .forEach(compositeModelTemplate::add);

            return Optional.of(compositeModelTemplate);
        }
        return Optional.empty();
    }

    @Override
    public int compareTo(@NotNull ModelMatcher o) {
        return 0;
    }
}
