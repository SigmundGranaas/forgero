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
import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.minecraft.common.service.StateMapper;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import lombok.Synchronized;

import net.minecraft.util.Identifier;

/**
 * Generates dynamic resources for Forgero using the DRP (Dynamic Resource Pack) module.
 * This replaces the former ARRP-based ARRPGenerator.
 */
public class ForgeroResourceGenerator {

	private static final List<DynamicResourceGenerator> generators = new ArrayList<>();
	private static DynamicResourcePack builtinPack;
	private static DynamicResourcePack dynamicPack;

	private final StateMapper mapper;

	public ForgeroResourceGenerator(StateMapper mapper) {
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

	/**
	 * Gets the builtin resource pack (for type tree tags and material tool tags).
	 * Creates it lazily if not already created.
	 */
	public static DynamicResourcePack getBuiltinPack() {
		if (builtinPack == null) {
			builtinPack = DRPApi.getInstance()
					.createPack("forgero:builtin_generator")
					.description("Forgero builtin generated resources")
					.build();
		}
		return builtinPack;
	}

	/**
	 * Gets the dynamic resource pack (for plugin-registered generators).
	 * Creates it lazily if not already created.
	 */
	public static DynamicResourcePack getDynamicPack() {
		if (dynamicPack == null) {
			dynamicPack = DRPApi.getInstance()
					.createPack("forgero:dynamic_generator")
					.description("Forgero dynamically generated resources")
					.build();
		}
		return dynamicPack;
	}

	public static void generate(StateService service) {
		new ForgeroResourceGenerator(service.getMapper()).generateResources();

		DynamicResourcePack pack = getDynamicPack();
		generators.stream()
				.filter(DynamicResourceGenerator::enabled)
				.forEach(generator -> generator.generate(pack));

		DRPApi api = DRPApi.getInstance();
		api.register(getBuiltinPack(), ResourcePackPhase.BEFORE_VANILLA);
		api.register(pack, ResourcePackPhase.BEFORE_VANILLA);
	}

	public void generateResources() {
		generateTagsFromStateTree();
		createMaterialToolTags();
	}

	public void generateTagsFromStateTree() {
		ForgeroStateRegistry.TREE.nodes().forEach(this::createTagFromType);
	}

	private void createTagFromType(MutableTypeNode node) {
		var states = node.getResources(State.class);
		if (!states.isEmpty()) {
			var tagBuilder = TagBuilder.items(Forgero.NAMESPACE + ":items/" + node.name().toLowerCase(Locale.ENGLISH));
			states.stream()
					.map(State::identifier)
					.forEach(id -> addToTag(id, tagBuilder));
			getBuiltinPack().addTag(tagBuilder);
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
			if (!states.isEmpty()) {
				var tagBuilder = TagBuilder.items(Forgero.NAMESPACE + ":items/" + key + "_tool");
				states.stream()
						.map(State::identifier)
						.forEach(id -> addToTag(id, tagBuilder));
				getBuiltinPack().addTag(tagBuilder);
			}
		}
	}

	private void addToTag(String id, TagBuilder<?> tagBuilder) {
		Optional<Identifier> tagId = mapper.stateToTag(id);
		if (tagId.isPresent()) {
			tagBuilder.includeTag(tagId.get().toString());
		} else {
			tagBuilder.add(mapper.stateToContainer(id).toString());
		}
	}
}
