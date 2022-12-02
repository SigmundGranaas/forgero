package com.sigmundgranaas.forgero.state;


import com.google.common.collect.ImmutableList;

public interface Upgradeable<T> extends State {
    T upgrade(State upgrade);

    ImmutableList<State> upgrades();
}
