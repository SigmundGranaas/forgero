package com.sigmundgranaas.drp.impl.pack;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.sigmundgranaas.drp.impl.resource.data.Recipe;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class DynamicServerPack extends DynamicResourcePack {
	public DynamicServerPack(String name) {
		super(name);
	}

	public void addRecipe(Identifier identifier, Recipe recipe) {
		var id = identifier;
		if (!identifier.toString().contains("recipes")) {
			id = new Identifier(identifier.getNamespace(), "recipes/" + identifier.getPath());
		}
		if (!identifier.toString().contains(".json")) {
			id = new Identifier(identifier.getNamespace(), id.getPath() + ".json");
		}
		Supplier<Resource> resource = () -> new Resource(getName(), recipe::asInputStream);
		this.addResource(id, resource);
	}


	@Override
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		if (type == ResourceType.SERVER_DATA) {
			return super.open(type, id);
		} else {
			throw new IOException("Invalid pack type!");
		}
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, Predicate<Identifier> allowedPathPredicate) {
		if (type == ResourceType.SERVER_DATA) {
			return super.findResources(type, namespace, prefix, allowedPathPredicate);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean contains(ResourceType type, Identifier id) {
		if (type == ResourceType.SERVER_DATA) {
			return super.contains(type, id);
		} else {
			return false;
		}
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		if (type == ResourceType.SERVER_DATA) {
			return super.getNamespaces(type);
		} else {
			return Collections.emptySet();
		}
	}
}
