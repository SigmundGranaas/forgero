package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class MaterialPartGroupTagGenerator implements DynamicResourceGenerator {
	private final StateService service;
	private final TypeTree tree;
	private final Map<String, List<String>> idTagEntries = new HashMap<>();
	private final List<String> materialGroups = List.of("WOOD", "STONE", "METAL");

	public MaterialPartGroupTagGenerator(StateService service, TypeTree tree) {
		this.service = service;
		this.tree = tree;
	}

	@Override
	public void generate(RuntimeResourcePack pack) {
		materialGroups.forEach(this::findPartsInGroup);
		for (Map.Entry<String, List<String>> entry : idTagEntries.entrySet()) {
			var tag = new JTag();
			entry.getValue().stream().map(Identifier::new).forEach(tag::add);
			pack.addTag(new Identifier(Forgero.NAMESPACE, "items/" + entry.getKey()), tag);
		}
	}

	private void findPartsInGroup(String type) {
		Set<State> materials = tree.find(Type.of(type))
				.map(node -> node.getResources(State.class))
				.map(HashSet::new)
				.orElse(new HashSet<>());

	}
}
