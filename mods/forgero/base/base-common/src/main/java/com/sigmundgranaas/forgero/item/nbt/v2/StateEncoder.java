package com.sigmundgranaas.forgero.item.nbt.v2;

import static com.sigmundgranaas.forgero.item.nbt.v2.CompositeEncoder.encodeConditions;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.*;

import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.nbt.NbtCompound;

public class StateEncoder implements CompoundEncoder<State> {
	private final IdentifiableEncoder identifiableEncoder;

	public StateEncoder() {
		this.identifiableEncoder = new IdentifiableEncoder();
	}

	@Override
	public NbtCompound encode(State element) {
		if (element instanceof Composite) {
			return new CompositeEncoder().encode(element);
		} else if (element instanceof LeveledState) {
			return new LeveledEncoder().encode(element);
		}
		var compound = identifiableEncoder.encode(element);
		if (element instanceof Conditional<?> conditional && conditional.localConditions().size() > 0) {
			compound.put(CONDITIONS_IDENTIFIER, encodeConditions(conditional));
		}
		compound.putString(STATE_TYPE_IDENTIFIER, STATE_IDENTIFIER);
		return compound;
	}

	public NbtCompound encode(PropertyContainer element) {
		if (element instanceof Composite composite) {
			return new CompositeEncoder().encode(composite);
		} else if (element instanceof LeveledState state) {
			return new LeveledEncoder().encode(state);
		}
		if (element instanceof Identifiable identifiable) {
			var compound = identifiableEncoder.encode(identifiable);
			if (element instanceof Conditional<?> conditional && conditional.localConditions().size() > 0) {
				compound.put(CONDITIONS_IDENTIFIER, encodeConditions(conditional));
			}
			compound.putString(STATE_TYPE_IDENTIFIER, STATE_IDENTIFIER);
			return compound;
		}

		return new NbtCompound();
	}
}
