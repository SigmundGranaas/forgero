package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class MaterialPartTagGenerator implements DynamicResourceGenerator {
	private final StateService service;
	private final Map<String, List<String>> idTagEntries = new HashMap<>();

	public MaterialPartTagGenerator(StateService service) {
		this.service = service;
	}

	@Override
	public void generate(RuntimeResourcePack pack) {
		service.all().stream()
				.map(Supplier::get)
				.filter(Composite.class::isInstance)
				.map(Composite.class::cast)
				.forEach(this::mapTags);

		for (Map.Entry<String, List<String>> entry : idTagEntries.entrySet()) {
			var tag = new JTag();
			entry.getValue().stream().map(Identifier::new).forEach(tag::add);
			pack.addTag(new Identifier(Forgero.NAMESPACE, "items/" + entry.getKey()), tag);
		}
	}

	private void mapTags(Composite construct) {
		var elements = construct.name().split(ELEMENT_SEPARATOR);
		if (elements.length > 1) {
			var tag = String.format("%s-%s", elements[0], construct.type().typeName().toLowerCase(Locale.ENGLISH));
			if (idTagEntries.containsKey(tag)) {
				idTagEntries.get(tag).add(construct.identifier());
			} else {
				idTagEntries.put(tag, new ArrayList<>(List.of(construct.identifier())));
			}
		}
	}
}
