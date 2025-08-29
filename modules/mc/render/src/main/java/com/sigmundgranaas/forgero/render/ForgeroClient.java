// FILE: /home/sigmund/Documents/projects/forgero/1-20/fabric/modules/render/src/main/java/com/sigmundgranaas/forgero/render/ForgeroClient.java
package com.sigmundgranaas.forgero.render;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.render.model.armor.ForgeroArmorModelManager;
import com.sigmundgranaas.forgero.render.model.armor.ForgeroArmorTextureManager;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;

public class ForgeroClient {
	/**
	 * A volatile holder for all client-side services.
	 * This ensures atomic updates during hot reloads, preventing the model provider
	 * from ever seeing a partially-loaded or inconsistent state.
	 */
	public static volatile ClientServices services;

	/**
	 * A record containing all necessary client-side registries and managers.
	 * An instance of this is created synchronously on startup, and a new one
	 * is created and atomically swapped in during a resource reload.
	 */
	public record ClientServices(
			ItemModelRegistry modelRegistry,
			ArmorModelRegistry armorModelRegistry,
			Function<ItemStack, Optional<Component>> itemToComponent,
			ComponentRegistry componentRegistry,
			ForgeroArmorTextureManager armorTextureManager,
			ForgeroArmorModelManager armorModelManager
	) {
	}
}
