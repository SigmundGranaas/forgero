package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.utils.StateUtils;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class PartTypeTagGenerator implements DynamicResourceGenerator {
	private final Map<String, List<String>> idTagEntries = new HashMap<>();

	@Override
	public void generate(RuntimeResourcePack pack) {
		ForgeroStateRegistry.STATES.all().stream()
				.map(Supplier::get)
				.filter(state -> state.test(Type.PART))
				.forEach(this::mapTags);
		for (Map.Entry<String, List<String>> entry : idTagEntries.entrySet()) {
			var tag = new JTag();
			entry.getValue().stream().map(Identifier::new).forEach(tag::add);
			pack.addTag(new Identifier(Forgero.NAMESPACE, "items/" + entry.getKey()), tag);
		}
	}

	private void mapTags(State construct) {
		var type = construct.type().typeName().toLowerCase();
		if (idTagEntries.containsKey(type)) {
			convertId(construct).ifPresent(id -> idTagEntries.get(type).add(id));
		} else {
			convertId(construct).ifPresent(id -> idTagEntries.put(type, new ArrayList<>(List.of(id))));
		}
	}

	private Optional<String> convertId(State state) {
		return StateUtils.containerMapper(state.identifier()).map(Identifier::toString);
	}
}
