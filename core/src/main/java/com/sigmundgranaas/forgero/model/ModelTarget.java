package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.util.MatchContext;
import com.sigmundgranaas.forgero.util.Matchable;

import java.util.List;

public class ModelTarget implements Matchable {
    private final List<String> target;
    private final ModelAssembly model;

    public ModelTarget(List<String> target, ModelAssembly model) {
        this.target = target;
        this.model = model;
    }

    @Override
    public boolean test(Matchable match) {
        if (match instanceof Composite composite) {
            return composite.ingredients().size() == target.size();
        }
        return false;
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        return test(match);
    }

    public ModelAssembly getModel() {
        return model;
    }
}
