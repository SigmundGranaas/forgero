package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.State;

import java.util.Optional;

public record TemplatedModelEntry(String template) implements ModelMatcher {

    @Override
    public Optional<ModelTemplate> find(State state, ModelProvider provider) {
        return provider.find(template).flatMap(matcher -> matcher.find(state, provider));
    }
}
