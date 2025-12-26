package com.sigmundgranaas.forgero.model.texture.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * DTO for Minecraft animation metadata from .mcmeta files.
 * <p>
 * Expected JSON format:
 * <pre>
 * {
 *   "animation": {
 *     "frametime": 2,
 *     "interpolate": true,
 *     "frames": [0, 1, 2, 3]
 *   }
 * }
 * </pre>
 *
 * @param frametime   The number of game ticks each frame is displayed (default: 1).
 * @param interpolate Whether to smoothly interpolate between frames (default: false).
 * @param frames      Optional explicit frame order. If null/empty, frames play sequentially.
 */
public record AnimationMetadataDTO(
		int frametime,
		boolean interpolate,
		@Nullable List<Integer> frames
) {
	/**
	 * Default animation metadata with frametime=1, no interpolation, and sequential frame order.
	 */
	public static final AnimationMetadataDTO DEFAULT = new AnimationMetadataDTO(1, false, null);

	/**
	 * Codec for the inner "animation" object content.
	 */
	public static final Codec<AnimationMetadataDTO> ANIMATION_CONTENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("frametime", 1).forGetter(AnimationMetadataDTO::frametime),
			Codec.BOOL.optionalFieldOf("interpolate", false).forGetter(AnimationMetadataDTO::interpolate),
			Codec.list(Codec.INT).optionalFieldOf("frames").forGetter(AnimationMetadataDTO::getFrames)
	).apply(instance, (frametime, interpolate, frames) ->
			new AnimationMetadataDTO(
					Math.max(1, frametime),
					interpolate,
					frames.orElse(null)
			)));

	/**
	 * Codec for the full .mcmeta file structure with "animation" wrapper.
	 */
	public static final Codec<AnimationMetadataDTO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ANIMATION_CONTENT_CODEC.fieldOf("animation").forGetter(dto -> dto)
	).apply(instance, dto -> dto));

	/**
	 * Helper method to get frames as Optional for codec compatibility.
	 */
	public Optional<List<Integer>> getFrames() {
		return Optional.ofNullable(frames);
	}

	/**
	 * Creates animation metadata with just a frametime.
	 */
	public static AnimationMetadataDTO withFrametime(int frametime) {
		return new AnimationMetadataDTO(Math.max(1, frametime), false, null);
	}

	/**
	 * Checks if this metadata specifies a custom frame order.
	 */
	public boolean hasCustomFrameOrder() {
		return frames != null && !frames.isEmpty();
	}

	/**
	 * Gets the frames list, returning an empty list if null.
	 */
	public List<Integer> framesOrEmpty() {
		return frames != null ? frames : List.of();
	}
}
