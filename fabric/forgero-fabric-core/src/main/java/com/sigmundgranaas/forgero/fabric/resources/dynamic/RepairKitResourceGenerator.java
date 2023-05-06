package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import static com.sigmundgranaas.forgero.minecraft.common.item.Items.EMPTY_REPAIR_KIT;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfiguration;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.lang.JLang;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.devtech.arrp.json.recipe.JIngredient;
import net.devtech.arrp.json.recipe.JIngredients;
import net.devtech.arrp.json.recipe.JResult;
import net.devtech.arrp.json.recipe.JShapelessRecipe;

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
	public void generate(RuntimeResourcePack pack) {
		createRepairKitsRecipes(pack);
		createRepairKitLang(pack);
		createRepairKitModel(pack);


	}

	private void createRepairKitsRecipes(RuntimeResourcePack pack) {
		var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build());
		for (State material : materials) {
			var ingredients = JIngredients.ingredients();
			ingredients.add(convertStateToIngredient(material));
			ingredients.add(JIngredient.ingredient().item(EMPTY_REPAIR_KIT));
			var recipe = JShapelessRecipe.shapeless(ingredients, JResult.result(new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit").toString()));
			pack.addRecipe(new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit"), recipe);

		}
	}

	private JIngredient convertStateToIngredient(State state) {
		var tagId = stateService.getMapper().stateToTag(state.identifier());
		if (tagId.isPresent()) {
			return JIngredient.ingredient().tag(tagId.get().toString());
		} else {
			return JIngredient.ingredient().item(stateService.getMapper().stateToContainer(state.identifier()).toString());
		}
	}

	private void createRepairKitModel(RuntimeResourcePack pack) {
		var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build());
		for (State material : materials) {
			var model = new JModel();
			model.parent("item/generated");
			//model.textures(new JTextures().layer0("forgero:item/base_repair_kit"));
			model.textures(new JTextures().layer0("forgero:item/repair_kit_leather_base")
					.layer1("forgero:item/repair_kit_needle_base")
					.layer2(String.format("forgero:item/%s-repair_kit", material.name())));
			pack.addModel(model, new Identifier(Forgero.NAMESPACE, "item/" + material.name() + "_repair_kit"));

		}
	}

	private void createRepairKitLang(RuntimeResourcePack pack) {
		var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
				.map(node -> node.getResources(State.class))
				.orElse(ImmutableList.<State>builder().build());
		var lang = new JLang();
		for (State material : materials) {
			if (ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(material.identifier())) {
				var name = material.name().substring(0, 1).toUpperCase() + material.name().substring(1);
				lang.item(new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit"), String.format("%s Repair kit", name));
			}
		}
		pack.addLang(new Identifier(Forgero.NAMESPACE, "en_us"), lang);
	}
}
