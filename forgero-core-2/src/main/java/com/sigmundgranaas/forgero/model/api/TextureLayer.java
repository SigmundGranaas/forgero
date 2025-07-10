package com.sigmundgranaas.forgero.model.api;

import java.util.Comparator;

public record TextureLayer(String texture, int order) implements Comparable<TextureLayer> {
	@Override public int compareTo(TextureLayer other) { return Comparator.comparingInt(TextureLayer::order).compare(this, other); }
}
