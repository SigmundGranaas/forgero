package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Construct;

import net.minecraft.nbt.NbtCompound;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.*;

public class LeveledEncoder implements CompoundEncoder<State> {
	private final IdentifiableEncoder identifiableEncoder;

	public LeveledEncoder() {
		this.identifiableEncoder = new IdentifiableEncoder();
	}

	@Override
	public NbtCompound encode(State element) {
		if (element instanceof Construct construct) {
			return new CompositeEncoder().encode(construct);
		}
		var compound = identifiableEncoder.encode(element);

		if (element instanceof LeveledState state) {
			compound.putInt(LEVEL_IDENTIFIER, state.level());
		}
		compound.putString(STATE_TYPE_IDENTIFIER, LEVELED_IDENTIFIER);
		return compound;
	}
}
