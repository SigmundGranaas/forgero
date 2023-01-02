package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.state.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.type.Type;

public class CompositeUpgrade extends Composite implements Upgrade {
    public CompositeUpgrade(Composite composite) {
        super(composite.ingredients(), SlotContainer.of(composite.slots()), composite.name(), composite.nameSpace(), composite.type());
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
