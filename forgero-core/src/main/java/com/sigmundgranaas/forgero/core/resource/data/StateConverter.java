package com.sigmundgranaas.forgero.core.resource.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.resource.data.factory.PropertyBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ConstructData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedSchematicPart;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.state.composite.StaticComposite;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.MutableTypeNode;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.core.util.Identifiers;

public class StateConverter implements DataConverter<State> {
	private final HashMap<String, State> states = new HashMap<>();
	private final TypeTree tree;
	private final HashMap<String, String> nameMapping = new HashMap<>();
	private final List<String> createStates = new ArrayList<>();

	public StateConverter(TypeTree tree) {
		this.tree = tree;
	}

	@Override
	public Optional<State> convert(DataResource resource) {
		if (resource.container().isPresent() && resource.container().get().getType().equals(Identifiers.CREATE_IDENTIFIER)) {
			createStates.add(resource.identifier());
		}
		if (resource.type().equals("GEM")) {
			return createGem(resource);
		} else if (resource.construct().isEmpty()) {
			return createState(resource);
		} else {
			return createComposite(resource);
		}
	}

	public HashMap<String, State> states() {
		return states;
	}

	public HashMap<String, String> nameMapper() {
		return nameMapping;
	}

	public List<String> createStates() {
		return createStates;
	}

	private Optional<State> createComposite(DataResource resource) {
		if (resource.construct().isPresent()) {
			State state;
			if (resource.construct().get().components().size() > 0) {
				state = buildTool(resource)
						.or(() -> buildSchematicPart(resource))
						.orElse(buildConstruct(resource));
			} else {
				state = buildStaticComposite(resource);
			}
			states.put(state.identifier(), state);
			nameMapping.put(resource.identifier(), state.identifier());
			return Optional.of(state);
		}
		return Optional.empty();
	}

	private State buildConstruct(DataResource resource) {
		var builder = Construct.builder(SlotContainer.of(createSlots(resource.construct().get())));
		builder.type(tree.type(resource.type()));
		builder.nameSpace(resource.nameSpace());
		//builder.name(resource.name());
		var ingredients = resource.construct()
				.map(ConstructData::components)
				.orElse(Collections.emptyList())
				.stream()
				.map(IngredientData::id)
				.map(nameMapping::get)
				.map(states::get)
				.filter(Objects::nonNull).toList();

		ingredients.forEach(builder::addIngredient);
		return builder.build();
	}

	private Optional<State> buildTool(DataResource resource) {
		var parts = resource.construct()
				.map(ConstructData::components)
				.orElse(Collections.emptyList())
				.stream()
				.map(IngredientData::id)
				.map(nameMapping::get)
				.map(states::get)
				.filter(Objects::nonNull).toList();
		var builderOpt = ConstructedTool.ToolBuilder.builder(parts);
		if (builderOpt.isPresent()) {
			var builder = builderOpt.get();
			var slotContainer = new SlotContainer(createSlots(resource.construct().get()));
			builder.addSlotContainer(slotContainer)
					.type(tree.type(resource.type()))
					.nameSpace(resource.nameSpace());
			return Optional.of(builder.build());

		}
		return Optional.empty();
	}

	private Optional<State> buildSchematicPart(DataResource resource) {
		var parts = resource.construct()
				.map(ConstructData::components)
				.orElse(Collections.emptyList())
				.stream()
				.map(IngredientData::id)
				.map(nameMapping::get)
				.map(states::get)
				.filter(Objects::nonNull).toList();
		var builderOpt = ConstructedSchematicPart.SchematicPartBuilder.builder(parts);
		if (builderOpt.isPresent()) {
			var builder = builderOpt.get();
			var slotContainer = new SlotContainer(createSlots(resource.construct().get()));
			builder.addSlotContainer(slotContainer)
					.type(tree.type(resource.type()))
					.nameSpace(resource.nameSpace());
			return Optional.of(builder.build());

		}
		return Optional.empty();
	}

	private State buildStaticComposite(DataResource resource) {
		var namespace = resource.nameSpace();
		var name = resource.name();
		var type = tree.type(resource.type());
		var properties = resource.properties()
				.map(PropertyBuilder::createPropertyListFromPOJO)
				.map(PropertyContainer::of)
				.orElse(PropertyContainer.of(Collections.emptyList()));

		var slotContainer = new SlotContainer(resource.construct().map(this::createSlots).orElse(new ArrayList<>()));
		return new StaticComposite(slotContainer, name, namespace, type, properties);
	}

	private List<Slot> createSlots(ConstructData data) {
		return IntStream.range(0, data.slots().size())
				.mapToObj(index -> new EmptySlot(index, tree.find(data.slots().get(index).type()).map(MutableTypeNode::type).orElse(Type.of(data.slots().get(index).type())), data.slots().get(index).description(), Set.copyOf(data.slots().get(index).category()))).collect(Collectors.toList());
	}

	private Optional<State> createState(DataResource resource) {
		var state = State.of(resource.name(),
				resource.nameSpace(),
				tree.type(resource.type()),
				resource.properties()
						.map(PropertyBuilder::createPropertyListFromPOJO)
						.orElse(Collections.emptyList()),
				resource.getCustomData());
		states.put(state.identifier(), state);
		nameMapping.put(resource.identifier(), state.identifier());
		return Optional.of(state);
	}

	private Optional<State> createGem(DataResource resource) {
		var gem = LeveledState
				.builder()
				.name(resource.name())
				.nameSpace(resource.nameSpace())
				.level(1)
				.type(tree.type(resource.type()))
				.properties(resource.properties()
						.map(PropertyBuilder::createPropertyListFromPOJO)
						.orElse(Collections.emptyList()))
				.build();
		states.put(gem.identifier(), gem);
		nameMapping.put(resource.identifier(), gem.identifier());
		return Optional.of(gem);
	}
}
