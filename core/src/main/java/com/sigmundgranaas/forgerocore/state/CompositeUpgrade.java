package com.sigmundgranaas.forgerocore.state;

import com.sigmundgranaas.forgerocore.type.Type;
import com.sigmundgranaas.forgerocore.util.MatchContext;
import com.sigmundgranaas.forgerocore.util.Matchable;

public class CompositeUpgrade extends Composite implements Upgrade {
    public CompositeUpgrade(Composite composite) {
        super(composite.ingredients(), composite.upgrades(), composite.name(), composite.type());
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
