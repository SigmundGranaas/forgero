package com.sigmundgranaas.forgero.resources.dynamic.impl;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.dynamicresourcepack.resource.DynamicResourcePackImpl;

import com.sigmundgranaas.forgero.resources.dynamic.DynamicResourceGenerator;

import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WoodPartsTag implements DynamicResourceGenerator {
	private final TypeTree tree;

	public WoodPartsTag(TypeTree tree) {
		this.tree = tree;
	}

	@Override
	public void generate(@NotNull DynamicResourcePackImpl pack) {
		var woods = tree.find(Type.of("WOOD"))
		                .map(node -> node.getResources(State.class))
		                .orElse(ImmutableList.<State>builder().build());
		var parts = List.of(
				"axe_head",
				"pickaxe_head",
				"shovel_head",
				"hoe_head",
				"sword_blade",
				"sword_guard",
				"handle"
		);
		for (var part : parts) {
			@NotNull var tag = new JsonArray();
			for (var wood : woods) {
				tag.add(new Identifier(Forgero.NAMESPACE, wood.name() + "-" + part).toString());
			}

			pack.put(new Identifier(Forgero.NAMESPACE, "items/" + "wood" + "-" + part), tag);
		}
	}
}
