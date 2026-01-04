package com.sigmundgranaas.forgero.utility.resource.loader.api;

import static com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConstants.*;

import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceProviderTextureAdapter;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * A unified interface for loading textures that bridges the existing texture loading pattern
 * with the {@link ResourceProvider} architecture.
 * <p>
 * UnifiedTextureProvider maintains the simple texture-by-identifier API while allowing
 * implementations to be backed by any ResourceProvider, enabling consistent resource
 * handling across the codebase.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create from a ResourceProvider
 * ResourceProvider assetProvider = CompositeResourceProvider.builder()
 *     .add(new MinecraftResourceProvider(resourceManager))
 *     .add(new ClassPathResourceProvider("assets"))
 *     .build();
 *
 * UnifiedTextureProvider textures = UnifiedTextureProvider.fromResourceProvider(
 *     assetProvider,
 *     "textures"
 * );
 *
 * // Load a texture
 * Optional<BufferedImage> pickaxeTexture = textures.getTexture("forgero:item/iron_pickaxe_head");
 * }</pre>
 *
 * @see ResourceProviderTextureAdapter
 */
public interface UnifiedTextureProvider {

	/**
	 * Gets a texture by its identifier.
	 * <p>
	 * The identifier should be in the format "namespace:path" where path is relative
	 * to the textures directory (e.g., "forgero:item/iron_pickaxe_head").
	 * The .png extension should NOT be included in the identifier.
	 *
	 * @param identifier The texture identifier (e.g., "minecraft:item/diamond")
	 * @return Optional containing the texture image, or empty if not found
	 */
	Optional<BufferedImage> getTexture(String identifier);

	/**
	 * Gets a texture by its ResourcePath.
	 *
	 * @param path The resource path to the texture
	 * @return Optional containing the texture image, or empty if not found
	 */
	default Optional<BufferedImage> getTexture(ResourcePath path) {
		return getTexture(path.namespace() + ":" + path.directory() + "/" + path.fileName());
	}

	/**
	 * Checks if a texture exists.
	 *
	 * @param identifier The texture identifier
	 * @return true if the texture exists
	 */
	default boolean hasTexture(String identifier) {
		return getTexture(identifier).isPresent();
	}

	/**
	 * Creates a UnifiedTextureProvider backed by a ResourceProvider.
	 *
	 * @param provider     The resource provider to use for loading
	 * @param texturesRoot The root directory for textures (e.g., "textures")
	 * @return A new UnifiedTextureProvider
	 */
	static UnifiedTextureProvider fromResourceProvider(ResourceProvider provider, String texturesRoot) {
		return new ResourceProviderTextureAdapter(provider, texturesRoot);
	}

	/**
	 * Creates a UnifiedTextureProvider with the default "textures" root.
	 *
	 * @param provider The resource provider to use for loading
	 * @return A new UnifiedTextureProvider
	 */
	static UnifiedTextureProvider fromResourceProvider(ResourceProvider provider) {
		return fromResourceProvider(provider, DIRECTORY_TEXTURES);
	}
}
