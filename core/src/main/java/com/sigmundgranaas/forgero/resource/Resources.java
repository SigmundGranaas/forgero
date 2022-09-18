package com.sigmundgranaas.forgero.resource;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.state.State;

public record Resources(ImmutableList<State> states) {

}
