package com.sigmundgranaas.forgero.model.validation.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parser and validator for Minecraft .mcmeta animation files.
 * <p>
 * mcmeta format:
 * <pre>
 * {
 *   "animation": {
 *     "frametime": 2,        // Ticks per frame (default: 1)
 *     "interpolate": true,   // Smooth transitions (default: false)
 *     "width": 16,           // Frame width (default: texture width)
 *     "height": 16,          // Frame height (default: texture width, making square)
 *     "frames": [0, 1, 2, {"index": 3, "time": 5}]  // Optional custom order
 *   }
 * }
 * </pre>
 */
public class McmetaParser {
	
	/**
	 * Parsed animation metadata.
	 */
	public record AnimationMeta(
			int frametime,
			boolean interpolate,
			Optional<Integer> width,
			Optional<Integer> height,
			List<FrameEntry> frames
	) {
		public static AnimationMeta defaults() {
			return new AnimationMeta(1, false, Optional.empty(), Optional.empty(), List.of());
		}
	}
	
	/**
	 * A frame entry in the animation.
	 */
	public record FrameEntry(int index, int time) {
		public FrameEntry(int index) {
			this(index, -1); // -1 means use default frametime
		}
	}
	
	/**
	 * Result of parsing a .mcmeta file.
	 */
	public record ParseResult(
			boolean success,
			Optional<AnimationMeta> meta,
			Optional<String> error
	) {
		public static ParseResult success(AnimationMeta meta) {
			return new ParseResult(true, Optional.of(meta), Optional.empty());
		}
		
		public static ParseResult failure(String error) {
			return new ParseResult(false, Optional.empty(), Optional.of(error));
		}
	}
	
	/**
	 * Parses a .mcmeta file from the given path.
	 *
	 * @param mcmetaPath Path to the .mcmeta file
	 * @return ParseResult containing the parsed metadata or an error
	 */
	public ParseResult parse(Path mcmetaPath) {
		if (!Files.exists(mcmetaPath)) {
			return ParseResult.failure("File does not exist: " + mcmetaPath);
		}
		
		try {
			String content = Files.readString(mcmetaPath);
			return parseContent(content);
		} catch (IOException e) {
			return ParseResult.failure("Could not read file: " + e.getMessage());
		}
	}
	
	/**
	 * Parses mcmeta content from a string.
	 *
	 * @param content The JSON content
	 * @return ParseResult containing the parsed metadata or an error
	 */
	public ParseResult parseContent(String content) {
		if (content == null || content.isBlank()) {
			return ParseResult.failure("Empty content");
		}
		
		try {
			JsonObject root = JsonParser.parseString(content).getAsJsonObject();
			
			if (!root.has("animation")) {
				return ParseResult.failure("Missing 'animation' key");
			}
			
			JsonObject animation = root.getAsJsonObject("animation");
			
			// Parse frametime (default: 1)
			int frametime = 1;
			if (animation.has("frametime")) {
				try {
					frametime = animation.get("frametime").getAsInt();
					if (frametime <= 0) {
						return ParseResult.failure("frametime must be positive, got: " + frametime);
					}
				} catch (Exception e) {
					return ParseResult.failure("Invalid frametime value: " + animation.get("frametime"));
				}
			}
			
			// Parse interpolate (default: false)
			boolean interpolate = false;
			if (animation.has("interpolate")) {
				try {
					interpolate = animation.get("interpolate").getAsBoolean();
				} catch (Exception e) {
					return ParseResult.failure("Invalid interpolate value: " + animation.get("interpolate"));
				}
			}
			
			// Parse width (optional)
			Optional<Integer> width = Optional.empty();
			if (animation.has("width")) {
				try {
					int w = animation.get("width").getAsInt();
					if (w <= 0) {
						return ParseResult.failure("width must be positive, got: " + w);
					}
					width = Optional.of(w);
				} catch (Exception e) {
					return ParseResult.failure("Invalid width value: " + animation.get("width"));
				}
			}
			
			// Parse height (optional)
			Optional<Integer> height = Optional.empty();
			if (animation.has("height")) {
				try {
					int h = animation.get("height").getAsInt();
					if (h <= 0) {
						return ParseResult.failure("height must be positive, got: " + h);
					}
					height = Optional.of(h);
				} catch (Exception e) {
					return ParseResult.failure("Invalid height value: " + animation.get("height"));
				}
			}
			
			// Parse frames (optional)
			List<FrameEntry> frames = new ArrayList<>();
			if (animation.has("frames")) {
				try {
					JsonArray framesArray = animation.getAsJsonArray("frames");
					for (int i = 0; i < framesArray.size(); i++) {
						var element = framesArray.get(i);
						if (element.isJsonPrimitive()) {
							frames.add(new FrameEntry(element.getAsInt()));
						} else if (element.isJsonObject()) {
							JsonObject frameObj = element.getAsJsonObject();
							int index = frameObj.get("index").getAsInt();
							int time = frameObj.has("time") ? frameObj.get("time").getAsInt() : -1;
							frames.add(new FrameEntry(index, time));
						} else {
							return ParseResult.failure("Invalid frame entry at index " + i);
						}
					}
				} catch (Exception e) {
					return ParseResult.failure("Invalid frames array: " + e.getMessage());
				}
			}
			
			return ParseResult.success(new AnimationMeta(frametime, interpolate, width, height, frames));
			
		} catch (JsonSyntaxException e) {
			return ParseResult.failure("Invalid JSON: " + e.getMessage());
		} catch (Exception e) {
			return ParseResult.failure("Parse error: " + e.getMessage());
		}
	}
	
	/**
	 * Validates that the animation metadata is compatible with the given texture dimensions.
	 *
	 * @param meta The parsed animation metadata
	 * @param textureWidth The texture width in pixels
	 * @param textureHeight The texture height in pixels
	 * @return Optional containing an error message if validation fails
	 */
	public Optional<String> validateAgainstTexture(AnimationMeta meta, int textureWidth, int textureHeight) {
		// Determine frame dimensions
		int frameWidth = meta.width().orElse(textureWidth);
		int frameHeight = meta.height().orElse(frameWidth); // Default to square frames
		
		// Check if texture height is divisible by frame height
		if (textureHeight % frameHeight != 0) {
			return Optional.of(String.format(
					"Texture height %d is not divisible by frame height %d",
					textureHeight, frameHeight
			));
		}
		
		int frameCount = textureHeight / frameHeight;
		
		// Validate frame indices if custom frames are specified
		if (!meta.frames().isEmpty()) {
			for (FrameEntry frame : meta.frames()) {
				if (frame.index() < 0 || frame.index() >= frameCount) {
					return Optional.of(String.format(
							"Frame index %d is out of bounds (0-%d)",
							frame.index(), frameCount - 1
					));
				}
				if (frame.time() != -1 && frame.time() <= 0) {
					return Optional.of(String.format(
							"Frame time must be positive, got %d for frame %d",
							frame.time(), frame.index()
					));
				}
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Calculates the number of frames in an animated texture.
	 *
	 * @param meta The parsed animation metadata
	 * @param textureWidth The texture width
	 * @param textureHeight The texture height
	 * @return The number of frames
	 */
	public int calculateFrameCount(AnimationMeta meta, int textureWidth, int textureHeight) {
		int frameHeight = meta.height().orElse(meta.width().orElse(textureWidth));
		return textureHeight / frameHeight;
	}
}
