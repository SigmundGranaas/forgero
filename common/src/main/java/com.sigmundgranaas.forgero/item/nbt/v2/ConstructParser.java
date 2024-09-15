package com.sigmundgranaas.forgero.item.nbt.v2;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.TYPE_IDENTIFIER;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.BaseComposite;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.type.Type;

import net.minecraft.nbt.NbtCompound;


public class ConstructParser extends CompositeParser {
	public ConstructParser(StateFinder supplier) {
		super(supplier);
	}

	@Override
	public Optional<State> parse(NbtCompound compound) {
		BaseComposite.BaseCompositeBuilder<?> builder;
		if (compound.contains(NbtConstants.ID_IDENTIFIER)) {
			var id = compound.getString(NbtConstants.ID_IDENTIFIER);
			var stateOpt = supplier.find(id);

			if (stateOpt.isPresent() && stateOpt.get() instanceof Construct construct) {
				var container = new SlotContainerParser(construct, new SlotParser(new StateParser(supplier)), new CompositeParser(supplier))
						.parse(compound);
				builder = Construct.builder(container.get());
			} else if (ForgeroStateRegistry.CONTAINER_TO_STATE.containsKey(id)) {
				return supplier.find(ForgeroStateRegistry.CONTAINER_TO_STATE.get(id));
			} else {
				builder = Construct.builder();
				builder.id(id);
			}
		} else {
			builder = Construct.builder();
			if (compound.contains(NbtConstants.NAME_IDENTIFIER)) {
				builder.name(compound.getString(NbtConstants.NAME_IDENTIFIER));
			}

			if (compound.contains(NbtConstants.NAMESPACE_IDENTIFIER)) {
				builder.nameSpace(compound.getString(NbtConstants.NAMESPACE_IDENTIFIER));
			}
		}
		if (compound.contains(TYPE_IDENTIFIER)) {
			builder.type(Type.of(compound.getString(TYPE_IDENTIFIER)));
		}

		parseParts(builder::addIngredient, compound);

		return Optional.of(builder.build());
	}
}
