package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.MatchContext;
import com.sigmundgranaas.forgero.util.Matchable;

import java.util.List;

public record ModelMatch(List<String> criteria, String matchType) implements Matchable {
    @Override
    public boolean test(Matchable match) {
        if (match instanceof Composite composite) {
            if (criteria.size() == composite.ingredients().size()) {
                return composite.ingredients().stream().allMatch(ingredient -> criteria.stream().anyMatch(criteria -> ingredientTest(ingredient, criteria)));
            } else if (matchType.equals("UPGRADE")) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        return false;
    }
    
    private boolean ingredientTest(State ingredient, String criteria) {
        if (criteria.contains("type")) {
            return ingredient.test(Type.of(criteria.replace("type:", "")));
        } else if (criteria.contains("id")) {
            return ingredient.identifier().contains(criteria.replace("id:", ""));
        }
        return false;
    }

    private boolean upgradeTest(State upgrade, String criteria) {
        return false;
    }
}
