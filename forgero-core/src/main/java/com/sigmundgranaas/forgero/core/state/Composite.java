package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;

import java.util.List;
import java.util.Optional;

public interface Composite extends Upgradeable<Composite>, State, CopyAble<Composite> {
    List<State> components();

    List<Property> compositeProperties(Target target);

    Optional<State> has(String id);
}
