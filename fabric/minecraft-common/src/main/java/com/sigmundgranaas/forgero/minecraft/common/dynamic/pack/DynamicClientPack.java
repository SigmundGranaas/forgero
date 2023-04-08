package com.sigmundgranaas.forgero.minecraft.common.dynamic.pack;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class DynamicClientPack extends DynamicResourcePack {
	public DynamicClientPack(String name) {
		super(new HashMap<>(), name);
	}

	@Override
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		if (type == ResourceType.CLIENT_RESOURCES) {
			return super.open(type, id);
		} else {
			throw new IOException("Invalid pack type!");
		}
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, Predicate<Identifier> allowedPathPredicate) {
		if (type == ResourceType.CLIENT_RESOURCES) {
			return super.findResources(type, namespace, prefix, allowedPathPredicate);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean contains(ResourceType type, Identifier id) {
		if (type == ResourceType.CLIENT_RESOURCES) {
			return super.contains(type, id);
		} else {
			return false;
		}
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		if (type == ResourceType.CLIENT_RESOURCES) {
			return super.getNamespaces(type);
		} else {
			return Collections.emptySet();
		}
	}
}
