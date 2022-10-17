package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public interface ModelMatcher {
    ModelMatcher EMPTY = new ModelMatcher() {
        @Override
        public boolean match(Matchable state) {
            return false;
        }

        @Override
        public Optional<ModelTemplate> get(Matchable state, ModelProvider provider) {
            return Optional.empty();
        }
    };

    static int comparator(ModelMatcher match1, ModelMatcher matcher2) {
        if (match1 instanceof ModelMatchPairing entry1 && matcher2 instanceof ModelMatchPairing entry2) {
            var match1Identifier = entry1.match().criteria().stream().anyMatch(match -> match.contains("id:"));
            var match2Identifier = entry2.match().criteria().stream().anyMatch(match -> match.contains("id:"));

            if (match1Identifier) {
                if (match2Identifier) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                if (match2Identifier) {
                    return 1;
                }
                return 0;
            }
        }
        return 0;
    }

    boolean match(Matchable state);

    Optional<ModelTemplate> get(Matchable state, ModelProvider provider);
}
