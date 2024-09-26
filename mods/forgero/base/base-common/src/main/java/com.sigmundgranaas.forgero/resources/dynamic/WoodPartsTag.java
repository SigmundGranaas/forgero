package com.sigmundgranaas.forgero.resources.dynamic;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

import java.util.List;


public class WoodPartsTag implements DynamicResourceGenerator {
	private final TypeTree tree;

	public WoodPartsTag(TypeTree tree) {
		this.tree = tree;
	}

	@Override
	public void generate(RuntimeResourcePack pack) {
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
			var tag = new JTag();
			for (var wood : woods) {
				tag.tag(new Identifier(Forgero.NAMESPACE, wood.name() + "-" + part));
			}

			pack.addTag(new Identifier(Forgero.NAMESPACE, "items/" + "wood" + "-" + part), tag);
		}
	}
}
