package com.sigmundgranaas.forgerocore.state;


import com.google.common.collect.ImmutableList;

public interface Upgradeable<T> extends State {
    T upgrade(Upgrade upgrade);

    ImmutableList<Upgrade> upgrades();
}
