package com.sigmundgranaas.forgero.model.rendering.api;

import com.sigmundgranaas.forgero.utility.resource.loader.api.UnifiedTextureProvider;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Legacy interface for texture loading.
 *
 * @deprecated Use {@link UnifiedTextureProvider} instead. This interface is maintained
 * for backward compatibility and will be removed in a future version.
 * <p>
 * Migration:
 * <pre>{@code
 * // Old approach
 * TextureProvider provider = ...;
 * Optional<BufferedImage> texture = provider.getTexture("forgero:item/iron_handle");
 *
 * // New approach
 * ResourceProvider resourceProvider = CompositeResourceProvider.builder()
 *     .add(new ClassPathResourceProvider("assets"))
 *     .build();
 * UnifiedTextureProvider textures = UnifiedTextureProvider.fromResourceProvider(resourceProvider, "textures");
 * Optional<BufferedImage> texture = textures.getTexture("forgero:item/iron_handle");
 * }</pre>
 *
 * @see UnifiedTextureProvider
 */
@Deprecated(since = "0.14.0", forRemoval = true)
public interface TextureProvider extends UnifiedTextureProvider {

	/**
	 * Gets a texture by its identifier.
	 *
	 * @param identifier The texture identifier (e.g., "forgero:item/iron_handle")
	 * @return Optional containing the texture image, or empty if not found
	 * @deprecated Use {@link UnifiedTextureProvider#getTexture(String)} instead
	 */
	@Override
	Optional<BufferedImage> getTexture(String identifier);
}
