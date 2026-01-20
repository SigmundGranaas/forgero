package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import static com.sigmundgranaas.forgero.minecraft.common.item.Items.EMPTY_REPAIR_KIT;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfiguration;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lang.LanguageBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.util.Identifier;

public class RepairKitResourceGenerator implements DynamicResourceGenerator {
	private final ForgeroConfiguration configuration;
	private final StateService stateService;

	public RepairKitResourceGenerator(ForgeroConfiguration configuration, StateService stateService) {
		this.configuration = configuration;
		this.stateService = stateService;
	}

	@Override
	public boolean enabled() {
		return configuration.enableRepairKits;
	}

	@Override
	public void generate(DynamicResourcePack pack) {
		createRepairKitsRecipes(pack);
		createRepairKitLang(pack);
		createRepairKitModel(pack);
	}

	private void createRepairKitsRecipes(DynamicResourcePack pack) {
		var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build());
		for (State material : materials) {
			var recipeId = new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit");
			pack.addShapelessRecipe(recipeId, builder -> {
				addIngredient(builder, material);
				builder.addIngredient(EMPTY_REPAIR_KIT.toString());
				builder.result(recipeId.toString());
			});
		}
	}

	private void addIngredient(ShapelessRecipeBuilder builder, State state) {
		var tagId = stateService.getMapper().stateToTag(state.identifier());
		if (tagId.isPresent()) {
			builder.addTagIngredient(tagId.get().toString());
		} else {
			builder.addIngredient(stateService.getMapper().stateToContainer(state.identifier()).toString());
		}
	}

	private void createRepairKitModel(DynamicResourcePack pack) {
		var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build());
		for (State material : materials) {
			pack.addModel(new Identifier(Forgero.NAMESPACE, "item/" + material.name() + "_repair_kit"), builder ->
					builder.parent("item/generated")
							.textures(tex -> tex
									.layer0("forgero:item/repair_kit_leather_base")
									.layer1("forgero:item/repair_kit_needle_base")
									.layer2(String.format("forgero:item/%s-repair_kit", material.name()))));
		}
	}

	private void createRepairKitLang(DynamicResourcePack pack) {
		var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build());
		LanguageBuilder langBuilder = LanguageBuilder.create();
		for (State material : materials) {
			if (StateService.INSTANCE.find(material.identifier()).isPresent()) {
				var name = material.name().substring(0, 1).toUpperCase() + material.name().substring(1).replace("_", " ");
				langBuilder.item(Forgero.NAMESPACE + ":" + material.name() + "_repair_kit", String.format("%s Repair kit", name));
			}
		}
		pack.addLanguage("en_us", langBuilder);
	}
}
