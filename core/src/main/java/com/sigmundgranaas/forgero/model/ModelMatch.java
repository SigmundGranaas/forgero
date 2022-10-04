package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.MatchContext;
import com.sigmundgranaas.forgero.util.match.Matchable;
import com.sigmundgranaas.forgero.util.match.NameMatch;

import java.util.List;

public record ModelMatch(List<String> criteria, String matchType) implements Matchable {
    @Override
    public boolean test(Matchable match) {
        if (match instanceof Composite composite) {
            if (criteria.size() == composite.ingredients().size()) {
                return composite.ingredients().stream().allMatch(ingredient -> criteria.stream().anyMatch(criteria -> ingredientTest(ingredient, criteria)));
            } else if (matchType.equals("UPGRADE")) {
                return composite.slots().stream().allMatch(ingredient -> criteria.stream().anyMatch(criteria -> upgradeTest(ingredient, criteria)));
            } else if (criteria.size() == 1) {
                if (ingredientTest(composite, criteria.get(0))) {
                    return true;
                } else {
                    return composite.ingredients().stream().anyMatch(ingredient -> ingredientTest(ingredient, criteria.get(0)));
                }
            }
        } else if (match instanceof State state) {
            return criteria.stream().allMatch(criteria -> ingredientTest(state, criteria));
        } else if (match instanceof Slot slot) {
            if (matchType.equals("UPGRADE")) {
                return criteria.stream().allMatch(criteria -> upgradeTest(slot, criteria));
            } else if (slot.get().isPresent() && criteria.size() == 1) {
                return ingredientTest(slot.get().get(), criteria.get(0));
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
        return ingredient.test(new NameMatch(criteria));
    }

    private boolean upgradeTest(Slot upgrade, String criteria) {
        if (upgrade.filled()) {
            if (criteria.contains("slot:")) {
                return criteria.replace("slot:", "").equals(String.valueOf(upgrade.index()));
            } else if (criteria.contains("type")) {
                return upgrade.test(Type.of(criteria.replace("type:", "")));
            }
            return upgrade.test(new NameMatch(criteria));
        }
        return false;
    }
}
