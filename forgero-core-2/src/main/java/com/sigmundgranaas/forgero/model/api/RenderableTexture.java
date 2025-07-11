package com.sigmundgranaas.forgero.model.api;

import java.util.Comparator;

/**
 * The final, resolved output of the ModelResolver.
 * Represents a single texture that needs to be rendered, with its order and position.
 *
 * @param texture The identifier of the texture to render.
 * @param order   The rendering order. Lower numbers are rendered first (further back).
 * @param offset  The positional offset for the texture.
 */
public record RenderableTexture(String texture, int order, Offset offset) implements Comparable<RenderableTexture> {
	@Override
	public int compareTo(RenderableTexture other) {
		return Comparator.comparingInt(RenderableTexture::order)
				.thenComparing(RenderableTexture::texture)
				.compare(this, other);
	}

	public RenderableTexture withOrder(int newOrder) {
		return new RenderableTexture(this.texture, newOrder, this.offset);
	}
}
