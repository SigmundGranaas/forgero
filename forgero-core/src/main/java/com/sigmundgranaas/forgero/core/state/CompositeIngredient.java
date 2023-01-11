package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class CompositeIngredient extends Construct implements Ingredient {
    public CompositeIngredient(Construct construct) {
        super(construct.ingredients(), SlotContainer.of(construct.slots()), construct.name(), construct.nameSpace(), construct.type());
    }

    @Override
    public boolean test(Matchable match, Context context) {
        if (match instanceof Type typeMatch) {
            if (this.type().test(typeMatch, context)) {
                return true;
            } else {
                return ingredients().stream().anyMatch(ingredient -> ingredient.test(match, context));
            }
        }
        return match.test(this, context);
    }
}
