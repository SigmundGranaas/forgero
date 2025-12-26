package com.sigmundgranaas.forgero.model.texture.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AnimationMetadataDTOTest {

	@Test
	void testParseSimpleAnimation() {
		String json = """
				{
				  "animation": {
				    "frametime": 2
				  }
				}
				""";

		JsonElement jsonElement = JsonParser.parseString(json);
		Optional<AnimationMetadataDTO> result = AnimationMetadataDTO.CODEC.parse(JsonOps.INSTANCE, jsonElement).result();

		assertTrue(result.isPresent());
		assertEquals(2, result.get().frametime());
		assertFalse(result.get().interpolate());
		assertNull(result.get().frames());
	}

	@Test
	void testParseFullAnimation() {
		String json = """
				{
				  "animation": {
				    "frametime": 5,
				    "interpolate": true,
				    "frames": [0, 1, 2, 3, 2, 1]
				  }
				}
				""";

		JsonElement jsonElement = JsonParser.parseString(json);
		Optional<AnimationMetadataDTO> result = AnimationMetadataDTO.CODEC.parse(JsonOps.INSTANCE, jsonElement).result();

		assertTrue(result.isPresent());
		assertEquals(5, result.get().frametime());
		assertTrue(result.get().interpolate());
		assertEquals(List.of(0, 1, 2, 3, 2, 1), result.get().frames());
	}

	@Test
	void testParseEmptyAnimationObject() {
		String json = """
				{
				  "animation": {}
				}
				""";

		JsonElement jsonElement = JsonParser.parseString(json);
		Optional<AnimationMetadataDTO> result = AnimationMetadataDTO.CODEC.parse(JsonOps.INSTANCE, jsonElement).result();

		assertTrue(result.isPresent());
		assertEquals(1, result.get().frametime()); // Default
		assertFalse(result.get().interpolate()); // Default
		assertNull(result.get().frames()); // Default
	}

	@Test
	void testSerializeSimple() {
		AnimationMetadataDTO metadata = new AnimationMetadataDTO(2, false, null);

		Optional<JsonElement> result = AnimationMetadataDTO.CODEC.encodeStart(JsonOps.INSTANCE, metadata).result();

		assertTrue(result.isPresent());
		String json = result.get().toString();
		assertTrue(json.contains("\"frametime\":2"));
		assertTrue(json.contains("\"animation\""));
	}

	@Test
	void testSerializeWithInterpolation() {
		AnimationMetadataDTO metadata = new AnimationMetadataDTO(3, true, null);

		Optional<JsonElement> result = AnimationMetadataDTO.CODEC.encodeStart(JsonOps.INSTANCE, metadata).result();

		assertTrue(result.isPresent());
		String json = result.get().toString();
		assertTrue(json.contains("\"frametime\":3"));
		assertTrue(json.contains("\"interpolate\":true"));
	}

	@Test
	void testSerializeWithFrames() {
		AnimationMetadataDTO metadata = new AnimationMetadataDTO(1, false, List.of(0, 1, 2, 1, 0));

		Optional<JsonElement> result = AnimationMetadataDTO.CODEC.encodeStart(JsonOps.INSTANCE, metadata).result();

		assertTrue(result.isPresent());
		String json = result.get().toString();
		assertTrue(json.contains("\"frames\":[0,1,2,1,0]"));
	}

	@Test
	void testRoundTrip() {
		AnimationMetadataDTO original = new AnimationMetadataDTO(4, true, List.of(0, 1, 2));

		// Encode
		Optional<JsonElement> encoded = AnimationMetadataDTO.CODEC.encodeStart(JsonOps.INSTANCE, original).result();
		assertTrue(encoded.isPresent());

		// Decode
		Optional<AnimationMetadataDTO> decoded = AnimationMetadataDTO.CODEC.parse(JsonOps.INSTANCE, encoded.get()).result();

		assertTrue(decoded.isPresent());
		assertEquals(original.frametime(), decoded.get().frametime());
		assertEquals(original.interpolate(), decoded.get().interpolate());
		assertEquals(original.frames(), decoded.get().frames());
	}

	@Test
	void testDefaultMetadata() {
		AnimationMetadataDTO defaultDto = AnimationMetadataDTO.DEFAULT;

		assertEquals(1, defaultDto.frametime());
		assertFalse(defaultDto.interpolate());
		assertNull(defaultDto.frames());
		assertFalse(defaultDto.hasCustomFrameOrder());
	}

	@Test
	void testWithFrametimeFactory() {
		AnimationMetadataDTO dto = AnimationMetadataDTO.withFrametime(5);

		assertEquals(5, dto.frametime());
		assertFalse(dto.interpolate());
		assertNull(dto.frames());
	}

	@Test
	void testNegativeFrametimeClampedByCodec() {
		// When parsed through the codec, negative frametime is clamped to 1
		String json = """
				{
				  "animation": {
				    "frametime": -5
				  }
				}
				""";

		JsonElement jsonElement = JsonParser.parseString(json);
		Optional<AnimationMetadataDTO> result = AnimationMetadataDTO.CODEC.parse(JsonOps.INSTANCE, jsonElement).result();

		assertTrue(result.isPresent());
		assertEquals(1, result.get().frametime()); // Clamped to 1 by codec
	}

	@Test
	void testWithFrametimeFactoryClampsNegative() {
		// Factory method also clamps negative values
		AnimationMetadataDTO dto = AnimationMetadataDTO.withFrametime(-5);

		assertEquals(1, dto.frametime());
	}

	@Test
	void testHasCustomFrameOrder() {
		AnimationMetadataDTO withFrames = new AnimationMetadataDTO(1, false, List.of(0, 1));
		AnimationMetadataDTO withoutFrames = new AnimationMetadataDTO(1, false, null);
		AnimationMetadataDTO emptyFrames = new AnimationMetadataDTO(1, false, List.of());

		assertTrue(withFrames.hasCustomFrameOrder());
		assertFalse(withoutFrames.hasCustomFrameOrder());
		assertFalse(emptyFrames.hasCustomFrameOrder());
	}

	@Test
	void testFramesOrEmpty() {
		AnimationMetadataDTO withFrames = new AnimationMetadataDTO(1, false, List.of(0, 1, 2));
		AnimationMetadataDTO withoutFrames = new AnimationMetadataDTO(1, false, null);

		assertEquals(List.of(0, 1, 2), withFrames.framesOrEmpty());
		assertEquals(List.of(), withoutFrames.framesOrEmpty());
	}
}
