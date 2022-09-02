package com.sigmundgranaas.forgero.core.state;


import com.google.common.collect.ImmutableList;

public interface Upgradeable extends State {
    Upgradeable upgrade(Upgrade upgrade);

    ImmutableList<Upgrade> upgrades();
}
