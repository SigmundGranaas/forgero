package com.sigmundgranaas.forgero.minecraft.common.dynamic.resource;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public interface DynamicResource {
	Resource resource();

	Identifier identifier();
}
