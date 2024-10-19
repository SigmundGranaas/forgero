package com.sigmundgranaas.forgero.resources.dynamic.impl;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.dynamicresourcepack.resource.DynamicResourcePackImpl;
import com.sigmundgranaas.forgero.resources.dynamic.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.service.StateService;

import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

public class MaterialPartTagGenerator implements DynamicResourceGenerator {
	private final @NotNull StateService service;
	private final @NotNull Map<String, List<String>> idTagEntries = new HashMap<>();

	public MaterialPartTagGenerator(@NotNull StateService service) {
		this.service = service;
	}

	@Override
	public void generate(@NotNull DynamicResourcePackImpl pack) {
		service.all().stream()
		       .map(Supplier::get)
		       .filter(Composite.class::isInstance)
		       .map(Composite.class::cast)
		       .forEach(this::mapTags);

		for (Map.Entry<String, List<String>> entry : idTagEntries.entrySet()) {
			@NotNull var tag = new JsonArray();
			entry.getValue().stream().map(Identifier::new).forEach(identifier -> tag.add(identifier.toString()));
			pack.put(new Identifier(Forgero.NAMESPACE, "items/" + entry.getKey()), tag);
		}
	}

	private void mapTags(@NotNull Composite construct) {
		var elements = construct.name().split(ELEMENT_SEPARATOR);
		if (elements.length <= 1) {
			return;
		}

		var tag = String.format("%s-%s", elements[0], construct.type().typeName().toLowerCase(Locale.ENGLISH));
		if (idTagEntries.containsKey(tag)) {
			idTagEntries.get(tag).add(construct.identifier());
		} else {
			idTagEntries.put(tag, new ArrayList<>(List.of(construct.identifier())));
		}
	}
}
