package com.sigmundgranaas.forgero.core.state;


import com.google.common.collect.ImmutableList;

import java.util.List;

public interface Upgradeable<T extends State> {
	T upgrade(State upgrade);

	ImmutableList<State> upgrades();

	T removeUpgrade(String id);

	boolean canUpgrade(State state);

	List<Slot> slots();
}
