package com.sigmundgranaas.forgero.core.texture.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Wrapper around RGB values in BufferedImage for making RGB value comparison easier
 */
public class RgbColour implements Comparable<RgbColour> {
	private final int red;
	private final int green;
	private final int blue;
	//Original value is kept because converting it back and forth using integers created some dumb errors
	private int original;


	public RgbColour(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public RgbColour(int rgba) {
		this.original = rgba;
		int red = (rgba >> 16) & 0xFF;
		int blue = (rgba >> 8) & 0xFF;
		int green = rgba & 0xFF;

		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public int getLightValue() {
		return (red + green + blue) / 3;
	}

	public int getRgb() {
		return original;
	}

	@Override
	public String toString() {
		return "RgbColour{" +
				"red=" + red +
				", green=" + green +
				", blue=" + blue +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RgbColour rgbColour = (RgbColour) o;

		if (red != rgbColour.red) return false;
		if (green != rgbColour.green) return false;
		return blue == rgbColour.blue;

	}

	@Override
	public int hashCode() {
		int result = red;
		result = 31 * result + green;
		result = 31 * result + blue;
		return result;
	}

	@Override
	public int compareTo(@NotNull RgbColour o) {
		return this.getLightValue() - o.getLightValue();
	}
}
