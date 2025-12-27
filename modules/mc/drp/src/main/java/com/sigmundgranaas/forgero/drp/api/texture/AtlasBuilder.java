package com.sigmundgranaas.forgero.drp.api.texture;

import com.sigmundgranaas.forgero.drp.impl.builder.AtlasBuilderImpl;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Builder for texture atlas entries.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * pack.addAtlas(
 *     new Identifier("minecraft", "blocks"),
 *     atlas -> atlas
 *         .addDirectory("forgero", "item/blades")
 *         .addSingle("forgero:item/special_texture")
 * );
 * }</pre>
 */
public interface AtlasBuilder {

	/**
	 * Creates a new atlas builder.
	 *
	 * @return A new builder instance
	 */
	static AtlasBuilder create() {
		return new AtlasBuilderImpl();
	}

	/**
	 * Adds a directory source to the atlas.
	 *
	 * @param namespace The texture namespace
	 * @param source    The source path prefix
	 * @return This builder for chaining
	 */
	AtlasBuilder addDirectory(String namespace, String source);

	/**
	 * Adds a directory source with a custom prefix.
	 *
	 * @param namespace The texture namespace
	 * @param source    The source path
	 * @param prefix    The sprite prefix in the atlas
	 * @return This builder for chaining
	 */
	AtlasBuilder addDirectory(String namespace, String source, String prefix);

	/**
	 * Adds a single texture to the atlas.
	 *
	 * @param textureId The texture identifier (namespace:path format)
	 * @return This builder for chaining
	 */
	AtlasBuilder addSingle(String textureId);

	/**
	 * Adds a single texture to the atlas.
	 *
	 * @param textureId The texture identifier
	 * @return This builder for chaining
	 */
	AtlasBuilder addSingle(Identifier textureId);

	/**
	 * Adds a single texture with a custom sprite name.
	 *
	 * @param textureId  The texture resource location
	 * @param spriteName The sprite identifier in the atlas
	 * @return This builder for chaining
	 */
	AtlasBuilder addSingle(Identifier textureId, Identifier spriteName);

	/**
	 * Adds a filter to exclude textures matching a pattern.
	 *
	 * @param namespacePattern Regex pattern for namespace (null for any)
	 * @param pathPattern      Regex pattern for path (null for any)
	 * @return This builder for chaining
	 */
	AtlasBuilder filter(String namespacePattern, String pathPattern);

	/**
	 * Gets all atlas sources.
	 *
	 * @return List of atlas sources
	 */
	List<AtlasSource> getSources();

	/**
	 * Represents a source entry in an atlas.
	 */
	interface AtlasSource {
		/**
		 * Gets the source type ("directory", "single", "filter", etc.).
		 */
		String type();
	}

	/**
	 * Directory source for atlas.
	 */
	interface DirectorySource extends AtlasSource {
		String source();
		String prefix();
	}

	/**
	 * Single texture source for atlas.
	 */
	interface SingleSource extends AtlasSource {
		Identifier resource();
		Identifier sprite();
	}

	/**
	 * Filter source for atlas.
	 */
	interface FilterSource extends AtlasSource {
		String namespacePattern();
		String pathPattern();
	}
}
