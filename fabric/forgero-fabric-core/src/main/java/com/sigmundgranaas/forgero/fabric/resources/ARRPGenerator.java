package com.sigmundgranaas.forgero.fabric.resources;


import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.MutableTypeNode;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.minecraft.common.service.StateMapper;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import lombok.Synchronized;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;


public class ARRPGenerator {

	public static final RuntimeResourcePack RESOURCE_PACK_BUILTIN = RuntimeResourcePack.create("forgero:builtin_generator");
	public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("forgero:dynamic_generator");
	private static final List<DynamicResourceGenerator> generators = new ArrayList<>();
	private final StateMapper mapper;

	public ARRPGenerator(StateMapper mapper) {
		this.mapper = mapper;
	}

	@Synchronized
	public static void register(DynamicResourceGenerator generator) {
		generators.add(generator);
	}

	@Synchronized
	public static void register(Supplier<DynamicResourceGenerator> supplier) {
		generators.add(supplier.get());
	}

	public static void generate(StateService service) {
		new ARRPGenerator(service.getMapper()).generateResources();
		generators.stream()
				.filter(DynamicResourceGenerator::enabled)
				.forEach(generator -> generator.generate(RESOURCE_PACK));
		RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK));
	}


	public void generateResources() {
		generateTagsFromStateTree();
		createMaterialToolTags();
		RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK_BUILTIN));
	}

	public void generateTagsFromStateTree() {
		ForgeroStateRegistry.TREE.nodes().forEach(this::createTagFromType);
	}

	private void createTagFromType(MutableTypeNode node) {
		JTag typeTag = new JTag();
		var states = node.getResources(State.class);
		if (states.size() > 0) {
			states.stream()
					.map(State::identifier)
					.forEach(id -> add(id, typeTag));
			RESOURCE_PACK_BUILTIN.addTag(new Identifier("forgero", "items/" + node.name().toLowerCase(Locale.ENGLISH)), typeTag);
		}
	}

	private void createMaterialToolTags() {
		var tools = ForgeroStateRegistry.STATES.find(Type.HOLDABLE);
		var materials = ForgeroStateRegistry.STATES.find(Type.TOOL_MATERIAL);

		Map<String, List<State>> materialMap = materials.stream()
				.map(Supplier::get)
				.collect(Collectors.toMap(Identifiable::name, material -> tools.stream().map(Supplier::get)
						.filter(tool -> Arrays.stream(tool.name().split(ELEMENT_SEPARATOR))
								.anyMatch(nameElement -> nameElement.equals(material.name())))
						.toList()));

		for (Map.Entry<String, List<State>> entry : materialMap.entrySet()) {
			String key = entry.getKey();
			List<State> states = entry.getValue();
			JTag materialToolTag = new JTag();
			if (states.size() > 0) {
				states.stream()
						.map(State::identifier)
						.forEach(id -> add(id, materialToolTag));
				RESOURCE_PACK_BUILTIN.addTag(new Identifier(Forgero.NAMESPACE, "items/" + key + "_tool"), materialToolTag);
			}
		}
	}

	private void add(String id, JTag tag) {
		Optional<Identifier> tagId = mapper.stateToTag(id);
		if (tagId.isPresent()) {
			tag.tag(tagId.get());
		} else {
			tag.add(mapper.stateToContainer(id));
		}
	}
}
