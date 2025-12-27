package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;

import java.util.List;


public class WoodPartsTag implements DynamicResourceGenerator {
	private final TypeTree tree;

	public WoodPartsTag(TypeTree tree) {
		this.tree = tree;
	}

	@Override
	public void generate(DynamicResourcePack pack) {
		var woods = tree.find(Type.of("WOOD"))
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build());

		var parts = List.of("axe_head",
				"pickaxe_head",
				"shovel_head",
				"hoe_head",
				"sword_blade",
				"sword_guard",
				"handle");
		for (var part : parts) {
			var tagBuilder = TagBuilder.items(Forgero.NAMESPACE + ":wood-" + part);
			for (var wood : woods) {
				tagBuilder.includeTag(Forgero.NAMESPACE + ":" + wood.name() + "-" + part);
			}
			pack.addTag(tagBuilder);
		}
	}
}
