package com.sigmundgranaas.forgero.core.util;

import java.util.Optional;

public interface Type extends Matchable {
    String typeName();

    boolean test(Matchable match);

    Optional<Type> parent();
}
