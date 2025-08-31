package com.sigmundgranaas.forgero.loader.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import net.minecraft.item.Item;

@FunctionalInterface
public interface ItemCreator {
	Item create(Component component, CreateData data, Resolver resolver);
}
