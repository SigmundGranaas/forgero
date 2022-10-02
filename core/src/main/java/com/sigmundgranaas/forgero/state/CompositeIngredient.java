package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.MatchContext;
import com.sigmundgranaas.forgero.util.match.Matchable;

public class CompositeIngredient extends Composite implements Ingredient {
    public CompositeIngredient(Composite composite) {
        super(composite.ingredients(), composite.upgrades(), composite.name(), composite.nameSpace(), composite.type());
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
