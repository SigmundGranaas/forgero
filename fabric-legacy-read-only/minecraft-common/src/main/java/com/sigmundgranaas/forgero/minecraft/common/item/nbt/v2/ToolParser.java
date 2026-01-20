package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.type.Type;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;


public class ToolParser extends CompositeParser {
	public ToolParser(StateFinder supplier) {
		super(supplier);
	}

	@Override
	public Optional<State> parse(NbtCompound compound) {
		List<State> parts = new ArrayList<>();
		parseParts(parts::add, compound);
		var id = compound.getString(NbtConstants.ID_IDENTIFIER);
		var stateOpt = supplier.find(id);
		if (parts.size() == 2) {
			var optBuilder = ConstructedTool.ToolBuilder.builder(parts);
			if (optBuilder.isPresent()) {
				var builder = optBuilder.get();
				builder.id(id);
				SlotParser slotParser = new SlotParser(new StateParser(supplier));
				CompositeParser compositeParser = new CompositeParser(supplier);
				if (stateOpt.isPresent() && stateOpt.get() instanceof Composite upgradeable) {
					new SlotContainerParser(upgradeable, slotParser, compositeParser)
							.parse(compound)
							.ifPresent(builder::addSlotContainer);
				} else {
					// Parse upgrades/slots even when state isn't in registry
					parseUpgrades(slotParser, compound, builder);
				}

				if (compound.contains(TYPE_IDENTIFIER)) {
					builder.type(Type.of(compound.getString(TYPE_IDENTIFIER)));
				}
				if (compound.contains(CONDITIONS_IDENTIFIER)) {
					parseConditions(compound.getList(CONDITIONS_IDENTIFIER, NbtElement.STRING_TYPE))
							.forEach(builder::condition);
				}
				return Optional.of(builder.build());
			}
		}
		return Optional.empty();
	}

	/**
	 * Parse upgrades/slots from the NBT and add them to the builder.
	 * This handles the case where the state isn't found in the registry but
	 * the NBT still contains valid slot/upgrade data.
	 */
	private void parseUpgrades(SlotParser slotParser, NbtCompound compound, ConstructedTool.ToolBuilder builder) {
		if (compound.contains(NbtConstants.UPGRADES_IDENTIFIER)) {
			var upgradesList = compound.getList(NbtConstants.UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < upgradesList.size(); i++) {
				NbtCompound slotNbt = upgradesList.getCompound(i);
				slotParser.parse(slotNbt).ifPresent(builder::addUpgrade);
			}
		}
	}

	private List<PropertyContainer> parseConditions(NbtList list) {
		List<PropertyContainer> conditions = new ArrayList<>();
		list.stream()
				.filter(element -> element.getType() == NbtElement.STRING_TYPE)
				.map(NbtElement::asString)
				.map(Conditions.INSTANCE::of)
				.flatMap(Optional::stream)
				.forEach(conditions::add);
		return conditions;
	}
}
