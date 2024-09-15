package com.sigmundgranaas.forgero.core.texture.V2;

import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class TextureLoader {
	private final FileLoader loader;

	public TextureLoader(FileLoader loader) {
		this.loader = loader;
	}

	Optional<BufferedImage> load(String location) {
		var streamOpt = loader.getStream(location);
		if (streamOpt.isPresent()) {
			var stream = streamOpt.get();
			try (stream) {
				return Optional.of(ImageIO.read(stream));
			} catch (Exception e) {
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	public Optional<JsonObject> loadMetadata(String name) {
		var streamOpt = loader.getStreamSilent(name);
		if (streamOpt.isPresent()) {
			var stream = streamOpt.get();
			try (stream) {
				var reader = new JsonReader(new InputStreamReader(stream));
				return Optional.of(new Gson().fromJson(reader, JsonObject.class));
			} catch (Exception e) {
				return Optional.empty();
			}
		}
		return Optional.empty();
	}
}
