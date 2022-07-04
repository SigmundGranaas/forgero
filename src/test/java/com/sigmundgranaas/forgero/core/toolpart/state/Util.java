package com.sigmundgranaas.forgero.core.toolpart.state;

import java.util.Optional;
import java.util.function.Supplier;

public class Util {
    public static Supplier<Optional<StateBuilder>> PICKAXEHEAD_STATE_BUILDER = () -> StateBuilder.of("oak", "pickaxe");
}
