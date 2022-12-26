package com.sigmundgranaas.forgero.state;

import java.util.function.Supplier;

@FunctionalInterface
public interface StateProvider extends Supplier<State> {
}
