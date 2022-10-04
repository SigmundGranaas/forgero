package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.List;
import java.util.Optional;

public class MatchedModelEntry implements ModelMatcher {
    List<ModelMatchPairing> models;
    String id;

    public MatchedModelEntry(List<ModelMatchPairing> models, String id) {
        this.models = models;
        this.id = id;
    }

    @Override
    public Optional<ModelTemplate> match(Matchable state, ModelProvider provider) {
        return models.stream()
                .filter(pairing -> pairing.match().test(state))
                .map(ModelMatchPairing::model)
                .map(pairing -> pairing.match(state, provider))
                .filter(Optional::isPresent)
                .flatMap(Optional::stream)
                .findFirst();
    }
}
