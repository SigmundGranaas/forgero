package com.sigmundgranaas.forgero.common.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import net.minecraft.item.Item;

@FunctionalInterface
public interface ItemCreator {
	Item create(Component component, CreateData data);
}
