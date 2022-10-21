package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import com.sigmundgranaas.forgero.util.match.NameMatch;
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
            var compositeContext = context.add(composite.type()).add(new NameMatch(composite.name()));
            composite.ingredients().stream()
                    .map(stateEntry ->
                            provider.find(stateEntry).filter(matcher -> matcher.match(stateEntry, compositeContext)).flatMap(matcher -> matcher.get(stateEntry, provider, compositeContext))
                    ).flatMap(Optional::stream)
                    .forEach(compositeModelTemplate::add);


            composite.slots().stream()
                    .filter(Slot::filled)
                    .map(slot ->
                            provider.find(composite).filter(matcher -> matcher.match(slot, context)).flatMap(matcher -> matcher.get(slot, provider, context))
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
