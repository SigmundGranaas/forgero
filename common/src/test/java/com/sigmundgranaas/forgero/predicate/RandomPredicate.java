package com.sigmundgranaas.forgero.predicate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.match.predicate.RandomPredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.minecraft.util.math.BlockPos;

class RandomPredicateTest {

	@Test
	void testSerializationDeserialization() {
		RandomPredicate predicate = new RandomPredicate(0.5f, 1200, Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS, RandomPredicate.SeedSource.WORLD_TIME));
		String json = RandomPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate).result().orElseThrow().toString();
		RandomPredicate deserializedPredicate = RandomPredicate.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElseThrow();

		assertEquals(predicate, deserializedPredicate);
	}

	@ParameterizedTest
	@MethodSource("provideValidConfigurations")
	void testValidConfigurations(float value, int worldTimeQuantization, List<RandomPredicate.SeedSource> seedSources) {
		RandomPredicate predicate = new RandomPredicate(value, worldTimeQuantization, seedSources);
		String json = RandomPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate).result().orElseThrow().toString();
		RandomPredicate deserializedPredicate = RandomPredicate.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElseThrow();

		assertEquals(predicate, deserializedPredicate);
		assertEquals(value, deserializedPredicate.value());
		assertEquals(worldTimeQuantization, deserializedPredicate.worldTimeQuantization());
		assertEquals(seedSources, deserializedPredicate.seedSources());
	}

	private static Stream<Arguments> provideValidConfigurations() {
		return Stream.of(
				Arguments.of(0.5f, 0, Collections.emptyList()),
				Arguments.of(0.3f, 1200, Collections.singletonList(RandomPredicate.SeedSource.WORLD_TIME)),
				Arguments.of(0.7f, 0, Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS, RandomPredicate.SeedSource.TARGET_ENTITY)),
				Arguments.of(1.0f, 0, Collections.singletonList(RandomPredicate.SeedSource.NONE)),
				Arguments.of(0.0f, 600, Arrays.asList(RandomPredicate.SeedSource.SOURCE_ENTITY, RandomPredicate.SeedSource.WORLD_TIME))
		);
	}

	@Test
	void baseRandomnessIsConsistent() {
		RandomPredicate predicate = new RandomPredicate(0.5f, 0, Collections.emptyList());
		MatchContext context = new MatchContext();

		int trueCount = 0;
		int iterations = 1000;

		for (int i = 0; i < iterations; i++) {
			if (predicate.test(null, context)) {
				trueCount++;
			}
		}

		// Check if the randomness is roughly 50% (allowing for some variance)
		assertTrue(trueCount > 400 && trueCount < 600, "Random distribution should be roughly 50%");
	}


	@Test
	void testExampleDeserialization() {
		RandomPredicate simple = decode(SIMPLE_RANDOM_EXAMPLE);
		assertEquals(0.5, simple.value());

		RandomPredicate seed = decode(SEEDED_RANDOM_EXAMPLE);
		assertEquals(0.5, seed.value());
		assertEquals(2, seed.seedSources().size());


		RandomPredicate quant = decode(SEEDED_QUANT_RANDOM_EXAMPLE);
		assertEquals(2, quant.seedSources().size());
		assertEquals(20, quant.worldTimeQuantization());
	}

	public static RandomPredicate decode(String predicate) {
		return RandomPredicate.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(predicate)).result().get().getFirst();
	}

	@Test
	public void testSeedCombination() {
		// Test that the same inputs always produce the same output
		long seed1 = combineSeedSources(5000, 1000);
		long seed2 = combineSeedSources(5000, 1000);
		assertEquals(seed1, seed2, "Same inputs should produce the same seed");

		// Test that different inputs produce different outputs
		long seed3 = combineSeedSources(5001, 1000);
		assertNotEquals(seed1, seed3, "Different inputs should produce different seeds");

		// Test that order matters
		long seed4 = combineSeedSources(1000, 5000);
		assertNotEquals(seed1, seed4, "Order of inputs should affect the final seed");
	}

	@Test
	public void testBlockPosSeedInfluence() {
		BlockPos pos = new BlockPos(5, 10, 15);
		long baseSeed = combineSeed(pos.asLong(), 1000);

		// Test that small changes in block position affect the final seed
		long seedX = combineSeed(pos.west().asLong(), 1000);
		long seedY = combineSeed(pos.up().asLong(), 1000);
		long seedZ = combineSeed(pos.north().asLong(), 1000);

		assertNotEquals(baseSeed, seedX, "Change in X should affect the seed");
		assertNotEquals(baseSeed, seedY, "Change in Y should affect the seed");
		assertNotEquals(baseSeed, seedZ, "Change in Z should affect the seed");
	}

	@Test
	public void testWorldTimeSeedInfluence() {
		int quantization = 100;
		long baseSeed = combineSeed(5000, computeWorldTimeSeed(1000, quantization));

		// Test that small changes in world time within the same quantization don't affect the seed
		long seed1 = combineSeed(5000, computeWorldTimeSeed(1050, quantization));
		assertEquals(baseSeed, seed1, "Small time change within quantization should not affect seed");

		// Test that changes across quantization boundaries do affect the seed
		long seed2 = combineSeed(5000, computeWorldTimeSeed(1100, quantization));
		assertNotEquals(baseSeed, seed2, "Time change across quantization should affect seed");
	}

	@Test
	public void testSeedDistribution() {
		// Test that seeds are well-distributed
		int buckets = 10;
		int[] distribution = new int[buckets];
		int iterations = 100000;

		for (int i = 0; i < iterations; i++) {
			long seed = combineSeed(i, i * 31);
			int bucket = (int) (Math.abs(seed) % buckets);
			distribution[bucket]++;
		}

		// Check that each bucket has a roughly equal number of seeds
		int expectedPerBucket = iterations / buckets;
		for (int count : distribution) {
			// Allow for 10% deviation from expected value
			assertTrue(Math.abs(count - expectedPerBucket) < expectedPerBucket * 0.1,
					"Seed distribution should be roughly uniform. Here are the buckets: " + Arrays.toString(distribution));
		}
	}

	private static final long SEED1 = 0x123456789ABCDEF0L;
	private static final long SEED2 = 0xFEDCBA9876543210L;
	private static final long SEED1_CHANGED = SEED1 + 1;


	@Test
	public void testSimpleAddition() {
		long result = simpleCombine(SEED1, SEED2);
		long resultChanged = simpleCombine(SEED1_CHANGED, SEED2);

		assertEquals(0x1111111111111100L, result, "Simple addition result is incorrect");
		assertEquals(1, Long.bitCount(result ^ resultChanged), "Simple addition should change only 1 bit");
	}

	@Test
	public void testSimpleXOR() {
		long result = simpleXOR(SEED1, SEED2);
		long resultChanged = simpleXOR(SEED1_CHANGED, SEED2);

		assertEquals(0xECE8ECE0ECE8ECE0L, result, "Simple XOR result is incorrect");
		assertEquals(1, Long.bitCount(result ^ resultChanged), "Simple XOR should change only 1 bit");
	}


	@Test
	public void testComparisonOfMethods() {
		long simpleAddResult = simpleCombine(SEED1, SEED2);
		long simpleXORResult = simpleXOR(SEED1, SEED2);
		long rotateXORResult = rotateAndXOR(SEED1, SEED2);

		assertNotEquals(simpleAddResult, simpleXORResult, "Simple addition and XOR should produce different results");
		assertNotEquals(simpleAddResult, rotateXORResult, "Simple addition and Rotate+XOR should produce different results");
		assertNotEquals(simpleXORResult, rotateXORResult, "Simple XOR and Rotate+XOR should produce different results");
	}

	@Test
	public void testSensitivityToSmallChanges() {
		long simpleAddOriginal = simpleCombine(SEED1, SEED2);
		long simpleXOROriginal = simpleXOR(SEED1, SEED2);
		long rotateXOROriginal = rotateAndXOR(SEED1, SEED2);

		long simpleAddChanged = simpleCombine(SEED1_CHANGED, SEED2);
		long simpleXORChanged = simpleXOR(SEED1_CHANGED, SEED2);
		long rotateXORChanged = rotateAndXOR(SEED1_CHANGED, SEED2);

		assertEquals(1, Long.bitCount(simpleAddOriginal ^ simpleAddChanged),
				"Simple addition should change 1 bit for small input change");
		assertEquals(1, Long.bitCount(simpleXOROriginal ^ simpleXORChanged),
				"Simple XOR should change 1 bit for small input change");
		assertTrue(Long.bitCount(rotateXOROriginal ^ rotateXORChanged) > 10,
				"Rotate and XOR should change a significant number of bits for small input change");
	}

	@Test
	public void testDistributionOfChanges() {
		int totalTests = 1000;
		int changesInAnyBit = 0;
		int changesInLowerBits = 0;
		int changesInUpperBits = 0;
		long upperMask = 0xFFFFFFFF00000000L; // Upper 32 bits
		long lowerMask = 0x00000000FFFFFFFFL; // Lower 32 bits

		for (int i = 0; i < totalTests; i++) {
			long seed1 = SEED1 + i;
			long rotateXOR1 = rotateAndXOR(seed1, SEED2);
			long rotateXOR2 = rotateAndXOR(seed1 + 1, SEED2);

			long diff = rotateXOR1 ^ rotateXOR2;

			if (diff != 0) {
				changesInAnyBit++;
				if ((diff & lowerMask) != 0) {
					changesInLowerBits++;
				}
				if ((diff & upperMask) != 0) {
					changesInUpperBits++;
				}
			}
		}

		assertTrue(changesInAnyBit == totalTests,
				"Rotate and XOR should change bits in every iteration");
		assertTrue(changesInLowerBits > totalTests * 0.9,
				"Rotate and XOR should affect lower bits in most iterations");
		assertTrue(changesInUpperBits > totalTests * 0.9,
				"Rotate and XOR should affect upper bits in most iterations");
	}

	@Test
	public void testVisualizeXOR() {
		long a = 0x123456789ABCDEF0L;
		long b = 0xFEDCBA9876543210L;
		long result = a ^ b;

		System.out.println("Visualizing XOR operation:");
		System.out.println("A:      " + toBinaryString(a));
		System.out.println("B:      " + toBinaryString(b));
		System.out.println("        ----------------------------------------------------------------");
		System.out.println("Result: " + toBinaryString(result));
		System.out.println("\nDetailed XOR operation:");

		for (int i = 0; i < 64; i += 8) {
			System.out.println("Bits " + i + "-" + (i + 7) + ":");
			System.out.println("A:      " + toBinaryString(a).substring(i, i + 8));
			System.out.println("B:      " + toBinaryString(b).substring(i, i + 8));
			System.out.println("        --------");
			System.out.println("Result: " + toBinaryString(result).substring(i, i + 8));
			System.out.println("Explanation: " + explainXOR(a, b, i, i + 8));
			System.out.println();
		}

		assertEquals(0xECE8ECE0ECE8ECE0L, result, "XOR result is incorrect");
	}

	private String toBinaryString(long n) {
		return String.format("%64s", Long.toBinaryString(n)).replace(' ', '0');
	}

	private String explainXOR(long a, long b, int start, int end) {
		StringBuilder explanation = new StringBuilder();
		for (int i = start; i < end; i++) {
			int bitA = (int) ((a >> (63 - i)) & 1);
			int bitB = (int) ((b >> (63 - i)) & 1);
			int resultBit = bitA ^ bitB;
			explanation.append(bitA).append("^").append(bitB).append("=").append(resultBit).append(" ");
		}
		return explanation.toString().trim();
	}
}
