package com.sigmundgranaas.drp.api;

import java.util.function.Consumer;

import com.sigmundgranaas.drp.impl.pack.DynamicResourcePack;
import lombok.Data;
import lombok.experimental.Accessors;

import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;

@Data
@Accessors(fluent = true)
public abstract class PackBuilder {
	private final String name;
	private final ResourceType type;
	private final Consumer<DynamicResourcePack> packConsumer;
	private final ResourcePackProfile.InsertionPosition position;


	public abstract DynamicResourcePack build();
}
