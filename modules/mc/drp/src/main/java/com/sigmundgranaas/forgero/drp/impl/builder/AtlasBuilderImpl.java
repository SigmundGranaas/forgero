package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.texture.AtlasBuilder;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of AtlasBuilder.
 */
public class AtlasBuilderImpl implements AtlasBuilder {

	private final List<AtlasSource> sources = new ArrayList<>();

	@Override
	public AtlasBuilder addDirectory(String namespace, String source) {
		return addDirectory(namespace, source, source);
	}

	@Override
	public AtlasBuilder addDirectory(String namespace, String source, String prefix) {
		sources.add(new DirectorySourceImpl(source, prefix));
		return this;
	}

	@Override
	public AtlasBuilder addSingle(String textureId) {
		Identifier id = Identifier.tryParse(textureId);
		return addSingle(id);
	}

	@Override
	public AtlasBuilder addSingle(Identifier textureId) {
		return addSingle(textureId, textureId);
	}

	@Override
	public AtlasBuilder addSingle(Identifier textureId, Identifier spriteName) {
		sources.add(new SingleSourceImpl(textureId, spriteName));
		return this;
	}

	@Override
	public AtlasBuilder filter(String namespacePattern, String pathPattern) {
		sources.add(new FilterSourceImpl(namespacePattern, pathPattern));
		return this;
	}

	@Override
	public List<AtlasSource> getSources() {
		return List.copyOf(sources);
	}

	/**
	 * Directory source implementation.
	 */
	public record DirectorySourceImpl(String source, String prefix) implements DirectorySource {
		@Override
		public String type() {
			return "directory";
		}
	}

	/**
	 * Single texture source implementation.
	 */
	public record SingleSourceImpl(Identifier resource, Identifier sprite) implements SingleSource {
		@Override
		public String type() {
			return "single";
		}
	}

	/**
	 * Filter source implementation.
	 */
	public record FilterSourceImpl(String namespacePattern, String pathPattern) implements FilterSource {
		@Override
		public String type() {
			return "filter";
		}
	}
}
