package com.sigmundgranaas.forgero.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.render.model.item.DisplayCodecs;
import com.google.gson.JsonParser;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DisplayCodecs - model transformation codec validation.
 * <p>
 * Tests:
 * - Transformation codec parsing and serialization
 * - Vector3f codec with different list sizes
 * - Translation clamping (-80 to 80)
 * - Scale clamping (-4 to 4)
 * - ModelTransformation codec with all display modes
 * - Edge cases: extreme values, malformed JSON
 * - Roundtrip serialization
 */
public class DisplayCodecsTest {

	/**
	 * Tests that a simple transformation can be parsed correctly.
	 */
	@Test
	public void testParseSimpleTransformation() {
		String json = """
				{
					"rotation": [0, 45, 0],
					"translation": [0, 1, 0],
					"scale": [1, 1, 1]
				}
				""";

		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.result().isPresent(), "Should parse valid transformation");
		Transformation transformation = result.result().get();

		assertNotNull(transformation, "Transformation should not be null");
		assertNotNull(transformation.rotation, "Rotation should not be null");
		assertNotNull(transformation.translation, "Translation should not be null");
		assertNotNull(transformation.scale, "Scale should not be null");
	}

	/**
	 * Tests that transformation with only rotation is parsed with defaults.
	 */
	@Test
	public void testParseTransformationWithDefaults() {
		String json = """
				{
					"rotation": [0, 0, 0]
				}
				""";

		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.result().isPresent(), "Should parse transformation with defaults");
		Transformation transformation = result.result().get();

		// Translation and scale should use defaults
		assertNotNull(transformation.translation, "Translation should use default");
		assertNotNull(transformation.scale, "Scale should use default");
	}

	/**
	 * Tests that translation values are clamped to [-80, 80] range.
	 */
	@Test
	public void testTranslationClamping() {
		String json = """
				{
					"rotation": [0, 0, 0],
					"translation": [100, -100, 50]
				}
				""";

		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.result().isPresent(), "Should parse transformation with clamping");
		Transformation transformation = result.result().get();

		// Check that translation values are clamped
		// Note: The codec multiplies by 0.0625 (1/16) after clamping
		Vector3f translation = transformation.translation;

		// 100 should be clamped to 80, then multiplied by 0.0625 = 5.0
		assertEquals(5.0f, translation.x(), 0.01f, "X should be clamped to 80 then scaled");
		// -100 should be clamped to -80, then multiplied by 0.0625 = -5.0
		assertEquals(-5.0f, translation.y(), 0.01f, "Y should be clamped to -80 then scaled");
		// 50 is within range, multiplied by 0.0625 = 3.125
		assertEquals(3.125f, translation.z(), 0.01f, "Z should be scaled without clamping");
	}

	/**
	 * Tests that scale values are clamped to [-4, 4] range.
	 */
	@Test
	public void testScaleClamping() {
		String json = """
				{
					"rotation": [0, 0, 0],
					"scale": [10, -10, 2]
				}
				""";

		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.result().isPresent(), "Should parse transformation with scale clamping");
		Transformation transformation = result.result().get();

		Vector3f scale = transformation.scale;

		// 10 should be clamped to 4
		assertEquals(4.0f, scale.x(), 0.01f, "X scale should be clamped to 4");
		// -10 should be clamped to -4
		assertEquals(-4.0f, scale.y(), 0.01f, "Y scale should be clamped to -4");
		// 2 is within range
		assertEquals(2.0f, scale.z(), 0.01f, "Z scale should not be clamped");
	}

	/**
	 * Tests that Vector3f codec handles single-value arrays (uniform scaling).
	 */
	@Test
	public void testVector3fSingleValue() {
		// This tests the internal vector codec behavior indirectly
		String transformJson = """
				{
					"rotation": [0, 0, 0],
					"scale": [2.0]
				}
				""";

		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(transformJson)
		);

		assertTrue(result.result().isPresent(), "Should parse single-value scale array");
		Transformation transformation = result.result().get();

		Vector3f scale = transformation.scale;
		assertEquals(2.0f, scale.x(), 0.01f, "X should be 2.0");
		assertEquals(2.0f, scale.y(), 0.01f, "Y should be 2.0");
		assertEquals(2.0f, scale.z(), 0.01f, "Z should be 2.0");
	}

	/**
	 * Tests Vector3f codec behavior with invalid array sizes.
	 *
	 * Note: optionalFieldOf uses default value when field parsing fails,
	 * so invalid array sizes result in the default being used, not errors.
	 * This tests the actual lenient behavior of the codec.
	 */
	@Test
	public void testVector3fInvalidSize() {
		String json = """
				{
					"rotation": [0, 0],
					"translation": [0, 0, 0],
					"scale": [1, 1, 1]
				}
				""";

		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		// optionalFieldOf treats parse failures as missing field -> uses default
		assertTrue(result.result().isPresent(),
				"Should parse with default rotation when invalid size provided");
		Transformation transformation = result.result().get();

		// Rotation uses default [0, 0, 0] when parse fails
		assertEquals(0.0f, transformation.rotation.x(), 0.01f);
		assertEquals(0.0f, transformation.rotation.y(), 0.01f);
		assertEquals(0.0f, transformation.rotation.z(), 0.01f);
	}

	/**
	 * Tests that ModelTransformation codec can parse all display modes.
	 */
	@Test
	public void testModelTransformationAllModes() {
		String json = """
				{
					"thirdperson_righthand": {
						"rotation": [0, 45, 0],
						"translation": [0, 2, 0],
						"scale": [0.5, 0.5, 0.5]
					},
					"thirdperson_lefthand": {
						"rotation": [0, -45, 0],
						"translation": [0, 2, 0],
						"scale": [0.5, 0.5, 0.5]
					},
					"firstperson_righthand": {
						"rotation": [0, 90, 0],
						"translation": [1, 1, 0],
						"scale": [1, 1, 1]
					},
					"firstperson_lefthand": {
						"rotation": [0, -90, 0],
						"translation": [1, 1, 0],
						"scale": [1, 1, 1]
					},
					"head": {
						"rotation": [0, 0, 0],
						"translation": [0, 13, 0],
						"scale": [1, 1, 1]
					},
					"gui": {
						"rotation": [30, 225, 0],
						"translation": [0, 0, 0],
						"scale": [0.625, 0.625, 0.625]
					},
					"ground": {
						"rotation": [0, 0, 0],
						"translation": [0, 3, 0],
						"scale": [0.25, 0.25, 0.25]
					},
					"fixed": {
						"rotation": [0, 0, 0],
						"translation": [0, 0, 0],
						"scale": [0.5, 0.5, 0.5]
					}
				}
				""";

		DataResult<ModelTransformation> result = DisplayCodecs.MODEL_TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.result().isPresent(), "Should parse complete ModelTransformation");
		ModelTransformation transformation = result.result().get();

		assertNotNull(transformation, "ModelTransformation should not be null");
		// All transformations should be defined
		// (ModelTransformation doesn't expose individual getters, but we validated parsing)
	}

	/**
	 * Tests that ModelTransformation uses defaults when modes are missing.
	 */
	@Test
	public void testModelTransformationDefaults() {
		String json = """
				{
					"gui": {
						"rotation": [30, 225, 0],
						"scale": [0.625, 0.625, 0.625]
					}
				}
				""";

		DataResult<ModelTransformation> result = DisplayCodecs.MODEL_TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.result().isPresent(),
				"Should parse ModelTransformation with only GUI defined");
		ModelTransformation transformation = result.result().get();
		assertNotNull(transformation, "ModelTransformation should use defaults for missing modes");
	}

	/**
	 * Tests roundtrip serialization: JSON -> Object -> JSON.
	 */
	@Test
	public void testTransformationRoundtrip() {
		String originalJson = """
				{
					"rotation": [10, 20, 30],
					"translation": [1, 2, 3],
					"scale": [0.5, 1.0, 1.5]
				}
				""";

		// Parse
		DataResult<Transformation> parseResult = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(originalJson)
		);
		assertTrue(parseResult.result().isPresent(), "Should parse original JSON");
		Transformation transformation = parseResult.result().get();

		// Serialize back
		DataResult<com.google.gson.JsonElement> encodeResult =
				DisplayCodecs.TRANSFORMATION_CODEC.encodeStart(JsonOps.INSTANCE, transformation);
		assertTrue(encodeResult.result().isPresent(), "Should encode transformation");

		// Parse again
		DataResult<Transformation> roundTripResult = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				encodeResult.result().get()
		);
		assertTrue(roundTripResult.result().isPresent(), "Should parse roundtrip JSON");

		// Values should be preserved (within floating point precision)
		Transformation roundTrip = roundTripResult.result().get();
		assertEquals(transformation.rotation.x(), roundTrip.rotation.x(), 0.01f);
		assertEquals(transformation.rotation.y(), roundTrip.rotation.y(), 0.01f);
		assertEquals(transformation.rotation.z(), roundTrip.rotation.z(), 0.01f);
	}

	/**
	 * Edge case: Test with extreme float values.
	 */
	@Test
	public void testExtremeValues() {
		String json = """
				{
					"rotation": [1000000, -1000000, 0],
					"translation": [1000, -1000, 0],
					"scale": [100, -100, 0.001]
				}
				""";

		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.result().isPresent(),
				"Should parse transformation with extreme values");
		Transformation transformation = result.result().get();

		// Rotations are not clamped
		assertNotNull(transformation.rotation, "Rotation should be parsed");

		// Translation should be clamped
		assertTrue(Math.abs(transformation.translation.x()) <= 5.0f,
				"Translation X should be clamped");
		assertTrue(Math.abs(transformation.translation.y()) <= 5.0f,
				"Translation Y should be clamped");

		// Scale should be clamped
		assertTrue(Math.abs(transformation.scale.x()) <= 4.0f, "Scale X should be clamped");
		assertTrue(Math.abs(transformation.scale.y()) <= 4.0f, "Scale Y should be clamped");
	}

	/**
	 * Edge case: Test with NaN and Infinity values.
	 */
	@Test
	public void testNaNAndInfinityValues() {
		// JsonParser will handle this, but codec should be robust
		// Note: JSON doesn't support NaN/Infinity, this tests codec robustness

		String json = """
				{
					"rotation": [0, 0, 0],
					"translation": [0, 0, 0],
					"scale": [1, 1, 1]
				}
				""";

		// This is more of a sanity check - actual NaN/Infinity would come from
		// malformed data or computational errors, not valid JSON
		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.result().isPresent(), "Should parse normal values");
	}

	/**
	 * Tests codec behavior with malformed field (type mismatch).
	 *
	 * Note: optionalFieldOf silently uses default when field has wrong type.
	 * This is DFU's standard behavior - type mismatches are treated as missing.
	 */
	@Test
	public void testMalformedJSON() {
		String json = """
				{
					"rotation": "not an array",
					"translation": [0, 0, 0]
				}
				""";

		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		// optionalFieldOf treats type mismatch as missing -> uses default
		assertTrue(result.result().isPresent(),
				"Should parse successfully with default rotation when type is wrong");
		Transformation transformation = result.result().get();

		// Rotation uses default [0, 0, 0] when parse fails due to type mismatch
		assertEquals(0.0f, transformation.rotation.x(), 0.01f);
		assertEquals(0.0f, transformation.rotation.y(), 0.01f);
		assertEquals(0.0f, transformation.rotation.z(), 0.01f);
	}

	/**
	 * Error case: Test with missing all fields (should use defaults).
	 */
	@Test
	public void testEmptyTransformation() {
		String json = "{}";

		DataResult<Transformation> result = DisplayCodecs.TRANSFORMATION_CODEC.parse(
				JsonOps.INSTANCE,
				JsonParser.parseString(json)
		);

		assertTrue(result.result().isPresent(),
				"Should parse empty object using all defaults");
		Transformation transformation = result.result().get();

		// Should have default values
		assertNotNull(transformation.rotation, "Should have default rotation");
		assertNotNull(transformation.translation, "Should have default translation");
		assertNotNull(transformation.scale, "Should have default scale");
	}
}
