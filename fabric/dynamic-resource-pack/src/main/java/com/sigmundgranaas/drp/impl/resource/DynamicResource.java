package com.sigmundgranaas.drp.impl.resource;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public interface DynamicResource {
	Resource resource();

	Identifier identifier();
}
