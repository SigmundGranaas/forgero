package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.state.slot.SlotContainer;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.MatchContext;
import com.sigmundgranaas.forgero.util.match.Matchable;

public class CompositeUpgrade extends Composite implements Upgrade {
    public CompositeUpgrade(Composite composite) {
        super(composite.ingredients(), SlotContainer.of(composite.slots()), composite.name(), composite.type());
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
