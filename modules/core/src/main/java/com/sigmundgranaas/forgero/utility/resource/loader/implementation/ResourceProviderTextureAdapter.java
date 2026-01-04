package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import static com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConstants.*;

import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.api.UnifiedTextureProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * Adapts a {@link ResourceProvider} to the {@link UnifiedTextureProvider} interface.
 * <p>
 * This adapter allows any ResourceProvider to be used for texture loading, providing
 * a unified approach to texture access across different resource sources (classpath,
 * filesystem, Minecraft ResourceManager, etc.).
 *
 * <h2>Identifier Format</h2>
 * <p>
 * Texture identifiers follow the format "namespace:path" where:
 * <ul>
 *   <li>namespace - The resource namespace (e.g., "forgero", "minecraft")</li>
 *   <li>path - The path within the textures directory, without .png extension</li>
 * </ul>
 * <p>
 * Example: "forgero:item/iron_pickaxe_head" resolves to
 * "{texturesRoot}/item/iron_pickaxe_head.png" in the "forgero" namespace.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * ResourceProvider provider = new ClassPathResourceProvider("assets");
 * UnifiedTextureProvider textures = new ResourceProviderTextureAdapter(provider, "textures");
 *
 * Optional<BufferedImage> image = textures.getTexture("forgero:item/iron_handle");
 * }</pre>
 */
public class ResourceProviderTextureAdapter implements UnifiedTextureProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceProviderTextureAdapter.class);

	private final ResourceProvider provider;
	private final String texturesRoot;

	/**
	 * Creates a new texture adapter.
	 *
	 * @param provider     The resource provider to use for loading
	 * @param texturesRoot The root directory for textures (e.g., "textures")
	 */
	public ResourceProviderTextureAdapter(ResourceProvider provider, String texturesRoot) {
		this.provider = Objects.requireNonNull(provider, "provider cannot be null");
		this.texturesRoot = normalizeRoot(Objects.requireNonNull(texturesRoot, "texturesRoot cannot be null"));
	}

	/**
	 * Creates a new texture adapter with the default "textures" root.
	 *
	 * @param provider The resource provider to use for loading
	 */
	public ResourceProviderTextureAdapter(ResourceProvider provider) {
		this(provider, DIRECTORY_TEXTURES);
	}

	@Override
	public Optional<BufferedImage> getTexture(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			LOGGER.warn("Invalid texture identifier: null or empty");
			return Optional.empty();
		}

		// Parse identifier
		int colonIndex = identifier.indexOf(':');
		if (colonIndex == -1) {
			LOGGER.warn("Invalid texture identifier format (missing namespace): {}", identifier);
			return Optional.empty();
		}

		String namespace = identifier.substring(0, colonIndex);
		String texturePath = identifier.substring(colonIndex + 1);

		if (namespace.isEmpty() || texturePath.isEmpty()) {
			LOGGER.warn("Invalid texture identifier (empty namespace or path): {}", identifier);
			return Optional.empty();
		}

		// Build the full path: texturesRoot/texturePath.png
		String fullPath = texturesRoot.isEmpty()
				? texturePath + "." + EXTENSION_PNG
				: texturesRoot + "/" + texturePath + "." + EXTENSION_PNG;

		// Parse into ResourcePath
		ResourcePath path = ResourcePath.fromIdentifier(
				new com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier(namespace, fullPath)
		);

		// Read and decode
		return provider.read(path).flatMap(stream -> decodeImage(stream, identifier));
	}

	@Override
	public Optional<BufferedImage> getTexture(ResourcePath path) {
		// For ResourcePath input, we expect the full path including extension
		ResourcePath texturePath = path.hasExtension(EXTENSION_PNG)
				? path
				: path.withExtension(EXTENSION_PNG);

		return provider.read(texturePath).flatMap(stream -> decodeImage(stream, texturePath.toString()));
	}

	@Override
	public boolean hasTexture(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			return false;
		}

		int colonIndex = identifier.indexOf(':');
		if (colonIndex == -1) {
			return false;
		}

		String namespace = identifier.substring(0, colonIndex);
		String texturePath = identifier.substring(colonIndex + 1);

		String fullPath = texturesRoot.isEmpty()
				? texturePath + "." + EXTENSION_PNG
				: texturesRoot + "/" + texturePath + "." + EXTENSION_PNG;

		ResourcePath path = ResourcePath.fromIdentifier(
				new com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier(namespace, fullPath)
		);

		return provider.exists(path);
	}

	/**
	 * Decodes an InputStream into a BufferedImage.
	 *
	 * @param stream     The input stream to decode
	 * @param identifier The texture identifier for logging context
	 */
	private Optional<BufferedImage> decodeImage(InputStream stream, String identifier) {
		try (InputStream is = stream) {
			BufferedImage image = ImageIO.read(is);
			if (image == null) {
				LOGGER.warn("Failed to decode image '{}': ImageIO returned null - file may be corrupted or not a valid image format",
						identifier);
				return Optional.empty();
			}
			return Optional.of(image);
		} catch (Exception e) {
			LOGGER.error("Failed to decode image '{}': {}", identifier, e.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * Normalizes the textures root by removing leading/trailing slashes.
	 */
	private static String normalizeRoot(String root) {
		if (root == null || root.isEmpty()) {
			return "";
		}
		return root.replaceAll("^/+|/+$", "");
	}
}
