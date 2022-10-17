package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import com.sigmundgranaas.forgero.util.match.NameMatch;

import java.util.List;

public record ModelMatch(List<String> criteria, String matchType) implements Matchable {
    @Override
    public boolean test(Matchable match, Context context) {
        if (match instanceof Composite composite) {
            var compositeMatch = context.add(composite);
            if (matchType.equals("UPGRADE")) {
                return composite.slots().stream().allMatch(slot -> criteria.stream().anyMatch(criteria -> upgradeTest(slot, criteria, compositeMatch)));
            } else if (criteria.size() == composite.ingredients().size()) {
                return composite.ingredients().stream().allMatch(ingredient -> criteria.stream().anyMatch(criteria -> ingredientTest(ingredient, criteria, compositeMatch)));
            } else if (criteria.size() == 1) {
                if (ingredientTest(composite, criteria.get(0), context)) {
                    return true;
                } else {
                    return composite.ingredients().stream().anyMatch(ingredient -> ingredientTest(ingredient, criteria.get(0), compositeMatch));
                }
            }
        } else if (match instanceof State state) {
            return criteria.stream().allMatch(criteria -> ingredientTest(state, criteria, context));
        } else if (match instanceof Slot slot) {
            if (matchType.equals("UPGRADE")) {
                return criteria.stream().allMatch(criteria -> upgradeTest(slot, criteria, context));
            } else if (slot.get().isPresent() && criteria.size() == 1) {
                return ingredientTest(slot.get().get(), criteria.get(0), context);
            }
        }
        return false;
    }

    private boolean ingredientTest(State ingredient, String criteria, Context context) {
        if (criteria.contains("type")) {
            var type = Type.of(criteria.replace("type:", ""));
            if (context.test(type, Context.of())) {
                return true;
            }
            return ingredient.test(type, context);
        } else if (criteria.contains("id")) {
            return ingredient.identifier().contains(criteria.replace("id:", ""));
        }
        return ingredient.test(new NameMatch(criteria), context);
    }

    private boolean upgradeTest(Slot upgrade, String criteria, Context context) {
        if (upgrade.filled()) {
            if (criteria.contains("slot:")) {
                return criteria.replace("slot:", "").equals(String.valueOf(upgrade.index()));
            } else if (criteria.contains("type")) {
                var type = Type.of(criteria.replace("type:", ""));
                if (context.test(type, Context.of())) {
                    return true;
                }
                return upgrade.test(Type.of(criteria.replace("type:", "")), context);
            }
            return upgrade.test(new NameMatch(criteria), context);
        }
        return false;
    }
}
