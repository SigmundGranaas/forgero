package com.sigmundgranaas.forgero.item.nbt.v2;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.ConditionedState;
import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Optional;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.*;
import static com.sigmundgranaas.forgero.item.nbt.v2.SchematicPartParser.parseConditions;

public class StateParser implements CompoundParser<State> {
	private final StateFinder supplier;
	private final CompositeParser compositeParser;
	private final LeveledParser leveledParser;

	public StateParser(StateFinder supplier) {
		this.supplier = supplier;
		this.compositeParser = new CompositeParser(supplier);
		this.leveledParser = new LeveledParser(supplier);
	}

	@Override
	public Optional<State> parse(NbtCompound compound) {
		if (!compound.contains(STATE_TYPE_IDENTIFIER)) {
			return Optional.empty();
		} else if (compound.getString(STATE_TYPE_IDENTIFIER).equals(COMPOSITE_IDENTIFIER)) {
			return compositeParser.parse(compound);
		} else if (compound.getString(STATE_TYPE_IDENTIFIER).equals(LEVELED_IDENTIFIER)) {
			return leveledParser.parse(compound);
		} else if (compound.contains(CONDITIONS_IDENTIFIER)) {
			var state = supplier.find(compound.getString(ID_IDENTIFIER));
			var conditions = parseConditions(compound.getList(CONDITIONS_IDENTIFIER, NbtElement.STRING_TYPE));
			if (state.isPresent()) {
				var conditioned = ConditionedState.of(state.get());
				for (PropertyContainer container : conditions) {
					conditioned = conditioned.applyCondition(container);
				}
				return Optional.of(conditioned);
			}
		}
		return supplier.find(compound.getString(ID_IDENTIFIER));
	}
}
