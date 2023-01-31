package com.sigmundgranaas.forgero.core.model;

import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ModelMatchPairing(ModelMatch match, ModelMatcher model) implements ModelMatcher {

    @Override
    public boolean match(Matchable state, Context context) {
        return match.test(state, context);
    }

    @Override
    public Optional<ModelTemplate> get(Matchable matchable, ModelProvider provider, Context context) {
        if (match.test(matchable, context)) {
            return model.get(matchable, provider, context);
        }
        return Optional.empty();
    }

    @Override
    public int compareTo(@NotNull ModelMatcher o) {
        if (o instanceof ModelMatchPairing pairing) {
            return pairing.match().criteria().size() - match().criteria().size();
        }
        return 0;
    }
}
