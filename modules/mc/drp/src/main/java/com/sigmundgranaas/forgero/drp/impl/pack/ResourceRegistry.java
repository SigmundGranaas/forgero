package com.sigmundgranaas.forgero.drp.impl.pack;

import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe storage for resource pack contents.
 */
public class ResourceRegistry {

	private final Map<Identifier, byte[]> dataResources = new ConcurrentHashMap<>();
	private final Map<Identifier, byte[]> assetResources = new ConcurrentHashMap<>();

	/**
	 * Adds a data resource (server-side: tags, recipes, etc.).
	 */
	public void addData(Identifier id, byte[] data) {
		dataResources.put(id, data);
	}

	/**
	 * Adds an asset resource (client-side: models, textures, lang, etc.).
	 */
	public void addAsset(Identifier id, byte[] data) {
		assetResources.put(id, data);
	}

	/**
	 * Gets a data resource.
	 */
	public byte[] getData(Identifier id) {
		return dataResources.get(id);
	}

	/**
	 * Gets an asset resource.
	 */
	public byte[] getAsset(Identifier id) {
		return assetResources.get(id);
	}

	/**
	 * Checks if a data resource exists.
	 */
	public boolean hasData(Identifier id) {
		return dataResources.containsKey(id);
	}

	/**
	 * Checks if an asset resource exists.
	 */
	public boolean hasAsset(Identifier id) {
		return assetResources.containsKey(id);
	}

	/**
	 * Gets all data resource identifiers.
	 */
	public Set<Identifier> getDataIds() {
		return Set.copyOf(dataResources.keySet());
	}

	/**
	 * Gets all asset resource identifiers.
	 */
	public Set<Identifier> getAssetIds() {
		return Set.copyOf(assetResources.keySet());
	}

	/**
	 * Gets all namespaces used in data resources.
	 */
	public Set<String> getDataNamespaces() {
		return dataResources.keySet().stream()
				.map(Identifier::getNamespace)
				.collect(java.util.stream.Collectors.toSet());
	}

	/**
	 * Gets all namespaces used in asset resources.
	 */
	public Set<String> getAssetNamespaces() {
		return assetResources.keySet().stream()
				.map(Identifier::getNamespace)
				.collect(java.util.stream.Collectors.toSet());
	}

	/**
	 * Clears all resources.
	 */
	public void clear() {
		dataResources.clear();
		assetResources.clear();
	}

	/**
	 * Gets the total number of resources.
	 */
	public int size() {
		return dataResources.size() + assetResources.size();
	}
}
