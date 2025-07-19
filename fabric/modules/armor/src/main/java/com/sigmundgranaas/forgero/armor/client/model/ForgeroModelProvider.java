package com.sigmundgranaas.forgero.armor.client.model;

import com.sigmundgranaas.forgero.armor.item.ForgeroHostItem;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ForgeroModelProvider implements ModelResolver {
	@Override
	public @Nullable UnbakedModel resolveModel(Context context) {
		Identifier resourceId = context.id(); // e.g., forgero:item/copper-chestplate

		if (resourceId.getNamespace().equals("forgero") && resourceId.getPath().startsWith("item/")) {

			// Convert the resource path ID to an item ID.
			// Path: "item/copper-chestplate" -> Item Path: "copper-chestplate"
			String itemPath = resourceId.getPath().substring("item/".length());
			Identifier itemId = new Identifier(resourceId.getNamespace(), itemPath);

			// Now, look up the item in the registry using the correct ID.
			Item item = Registries.ITEM.get(itemId);

			// If it's our item, we provide our custom model logic.
			// This successfully intercepts the load request.
			if (item instanceof ForgeroHostItem) {
				return new UnbakedForgeroModel();
			}
		}

		// For all other models (e.g., blocks, other mods' items), we do nothing.
		return null;
	}
}
