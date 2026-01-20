package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.TYPE_IDENTIFIER;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.BaseComposite;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.type.Type;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;


public class ConstructParser extends CompositeParser {
	public ConstructParser(StateFinder supplier) {
		super(supplier);
	}

	@Override
	public Optional<State> parse(NbtCompound compound) {
		BaseComposite.BaseCompositeBuilder<?> builder;
		SlotParser slotParser = new SlotParser(new StateParser(supplier));
		CompositeParser compositeParser = new CompositeParser(supplier);

		if (compound.contains(NbtConstants.ID_IDENTIFIER)) {
			var id = compound.getString(NbtConstants.ID_IDENTIFIER);
			var stateOpt = supplier.find(id);

			if (stateOpt.isPresent() && stateOpt.get() instanceof Construct construct) {
				var container = new SlotContainerParser(construct, slotParser, compositeParser)
						.parse(compound);
				builder = Construct.builder(container.get());
			} else if (ForgeroStateRegistry.CONTAINER_TO_STATE.containsKey(id)) {
				return supplier.find(ForgeroStateRegistry.CONTAINER_TO_STATE.get(id));
			} else {
				builder = Construct.builder();
				builder.id(id);
				// Parse upgrades/slots even when state isn't in registry
				parseUpgrades(slotParser, compound, builder);
			}
		} else {
			builder = Construct.builder();
			if (compound.contains(NbtConstants.NAME_IDENTIFIER)) {
				builder.name(compound.getString(NbtConstants.NAME_IDENTIFIER));
			}

			if (compound.contains(NbtConstants.NAMESPACE_IDENTIFIER)) {
				builder.nameSpace(compound.getString(NbtConstants.NAMESPACE_IDENTIFIER));
			}
			// Parse upgrades/slots for unnamed constructs too
			parseUpgrades(slotParser, compound, builder);
		}
		if (compound.contains(TYPE_IDENTIFIER)) {
			builder.type(Type.of(compound.getString(TYPE_IDENTIFIER)));
		}

		parseParts(builder::addIngredient, compound);

		return Optional.of(builder.build());
	}

	/**
	 * Parse upgrades/slots from the NBT and add them to the builder.
	 * This handles the case where the state isn't found in the registry but
	 * the NBT still contains valid slot/upgrade data.
	 */
	private void parseUpgrades(SlotParser slotParser, NbtCompound compound, BaseComposite.BaseCompositeBuilder<?> builder) {
		if (compound.contains(NbtConstants.UPGRADES_IDENTIFIER)) {
			var upgradesList = compound.getList(NbtConstants.UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < upgradesList.size(); i++) {
				NbtCompound slotNbt = upgradesList.getCompound(i);
				slotParser.parse(slotNbt).ifPresent(builder::addUpgrade);
			}
		}
	}
}
