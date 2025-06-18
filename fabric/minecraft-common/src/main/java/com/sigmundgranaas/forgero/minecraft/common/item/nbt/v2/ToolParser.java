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
				if (stateOpt.isPresent() && stateOpt.get() instanceof Composite upgradeable) {
					new SlotContainerParser(upgradeable, new SlotParser(new StateParser(supplier)), new CompositeParser(supplier))
							.parse(compound)
							.ifPresent(builder::addSlotContainer);
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
