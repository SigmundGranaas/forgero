package com.sigmundgranaas.forgero.resources.dynamic.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.dynamicresourcepack.resource.DynamicResourcePackImpl;
import com.sigmundgranaas.forgero.resources.dynamic.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.service.StateService;
import com.sigmundgranaas.forgero.utils.StateUtils;

import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

public class PartTypeTagGenerator implements DynamicResourceGenerator {
	private final @NotNull StateService service;
	private final @NotNull Map<String, List<String>> idTagEntries = new HashMap<>();

	public PartTypeTagGenerator(@NotNull StateService service) {
		this.service = service;
	}

	@Override
	public void generate(@NotNull DynamicResourcePackImpl pack) {
		service.all().stream()
		       .map(Supplier::get)
		       .filter(state -> state.test(Type.PART))
		       .forEach(this::mapTags);
		for (Map.Entry<String, List<String>> entry : idTagEntries.entrySet()) {
			@NotNull var tag = new JsonArray();
			entry.getValue().stream().map(Identifier::new).forEach(identifier -> tag.add(identifier.toString()));
			pack.put(new Identifier(Forgero.NAMESPACE, "items/" + entry.getKey()), tag);
		}
	}

	private void mapTags(@NotNull State construct) {
		var type = construct.type().typeName().toLowerCase(Locale.ENGLISH);
		if (idTagEntries.containsKey(type)) {
			convertId(construct).ifPresent(id -> idTagEntries.get(type).add(id));
		} else {
			convertId(construct).ifPresent(id -> idTagEntries.put(type, new ArrayList<>(List.of(id))));
		}
	}

	private Optional<String> convertId(@NotNull State state) {
		return StateUtils.containerMapper(state.identifier()).map(Identifier::toString);
	}
}
