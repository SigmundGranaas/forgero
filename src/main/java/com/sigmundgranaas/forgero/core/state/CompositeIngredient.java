package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.util.MatchContext;
import com.sigmundgranaas.forgero.core.util.Matchable;
import com.sigmundgranaas.forgero.core.util.Type;

import java.util.List;

public class CompositeIngredient extends Composite implements Ingredient {
    protected CompositeIngredient(List<Ingredient> ingredientList, String name, Type type) {
        super(ingredientList, name, type);
    }

    public CompositeIngredient(Composite composite) {
        super(composite.ingredients(), composite.name(), composite.type());
    }

    @Override
    public boolean test(Matchable match) {
        if (match instanceof Type typeMatch) {
            if (this.type().test(typeMatch)) {
                return true;
            } else {
                return ingredients().stream().anyMatch(ingredient -> ingredient.test(match, MatchContext.COMPOSITE));
            }
        }
        return match.test(this);
    }
}
