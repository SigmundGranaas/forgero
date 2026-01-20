package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.minecraft.common.utils.StateUtils;

import net.minecraft.util.Identifier;

public class PartTypeTagGenerator implements DynamicResourceGenerator {
	private final StateService service;
	private final Map<String, List<String>> idTagEntries = new HashMap<>();

	public PartTypeTagGenerator(StateService service) {
		this.service = service;
	}

	@Override
	public void generate(DynamicResourcePack pack) {
		service.all().stream()
				.map(Supplier::get)
				.filter(state -> state.test(Type.PART))
				.forEach(this::mapTags);
		for (Map.Entry<String, List<String>> entry : idTagEntries.entrySet()) {
			var tagBuilder = TagBuilder.items(Forgero.NAMESPACE + ":" + entry.getKey());
			entry.getValue().forEach(tagBuilder::add);
			pack.addTag(tagBuilder);
		}
	}

	private void mapTags(State construct) {
		var type = construct.type().typeName().toLowerCase(Locale.ENGLISH);
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
