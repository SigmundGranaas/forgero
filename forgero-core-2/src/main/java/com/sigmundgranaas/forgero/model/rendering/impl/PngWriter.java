package com.sigmundgranaas.forgero.model.rendering.impl;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PngWriter {
	public static void save(BufferedImage image, String path) throws IOException {
		File outputFile = new File(path);
		outputFile.getParentFile().mkdirs();
		ImageIO.write(image, "PNG", outputFile);
	}
}
