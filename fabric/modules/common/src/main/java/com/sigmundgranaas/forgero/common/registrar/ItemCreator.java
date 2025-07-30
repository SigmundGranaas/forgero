package com.sigmundgranaas.forgero.common.registrar;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import net.minecraft.item.Item;

/**
 * A functional interface for creating a Minecraft Item from a Forgero Component.
 * Implementations of this interface contain the specific logic for instantiating
 * module-specific items like armor or tools.
 */
@FunctionalInterface
public interface ItemCreator {
	/**
	 * Creates an Item instance.
	 *
	 * @param component  The resolved Forgero component that this item represents.
	 * @param createData The data from the "create" block of the host data file.
	 * @param resolver   The resolver engine for calculating final attribute values.
	 * @return A new Minecraft Item instance.
	 */
	Item create(Component component, CreateData createData, Resolver resolver);
}
