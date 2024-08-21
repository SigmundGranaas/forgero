package com.sigmundgranaas.forgero.smithing.recipe;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipes {
	public static void registerRecipes() {
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(Forgero.NAMESPACE, MetalMoldRecipe.Serializer.ID),
				MetalMoldRecipe.Serializer.INSTANCE);
		Registry.register(Registries.RECIPE_TYPE, new Identifier(Forgero.NAMESPACE, MetalMoldRecipe.Type.ID),
				MetalMoldRecipe.Type.INSTANCE);

		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(Forgero.NAMESPACE, MetalSmeltingRecipe.Serializer.ID),
				MetalSmeltingRecipe.Serializer.INSTANCE);
		Registry.register(Registries.RECIPE_TYPE, new Identifier(Forgero.NAMESPACE, MetalSmeltingRecipe.Type.ID),
				MetalSmeltingRecipe.Type.INSTANCE);
	}
}
