package com.sigmundgranaas.forgero.armor;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;

/**
 * A static container for holding client-side singletons and registries.
 * This class is populated during client initialization and serves as a
 * simple form of dependency injection for mixins and other classes that
 * don't have access to the full application context.
 */
public class ForgeroClient {
	public static ItemModelRegistry modelRegistry;
	public static ArmorModelRegistry armorModelRegistry;
	public static Function<ItemStack, Optional<Component>> itemToComponent;
}
