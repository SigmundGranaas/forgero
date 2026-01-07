package com.sigmundgranaas.forgero.common.nbt;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComponentNbtConverterTest {

	private ComponentNbtConverter converter;
	private Component testComponent;
	private OpenIdentifier testId;

	@BeforeEach
	void setUp() {
		testId = OpenIdentifier.parse("forgero:test_component");
		testComponent = new StaticComponent(
				testId,
				Set.of(OpenIdentifier.parse("forgero:test_tag")),
				Map.of()
		);

		Codec<Component> mockCodec = Codec.STRING.xmap(
				id -> testComponent,
				comp -> comp.id().toString()
		);
		converter = new ComponentNbtConverter(mockCodec);
	}

	@Nested
	class FromNbt {

		@Test
		void returnsEmptyWhenNbtIsNull() {
			Optional<Component> result = converter.fromNbt(null);

			assertTrue(result.isEmpty());
		}

		@Test
		void returnsEmptyWhenKeyIsMissing() {
			NbtCompound nbt = new NbtCompound();

			Optional<Component> result = converter.fromNbt(nbt);

			assertTrue(result.isEmpty());
		}

		@Test
		void decodesComponentFromStringNbt() {
			NbtCompound nbt = new NbtCompound();
			nbt.putString(ComponentNbtConverter.FORGERO_NBT_KEY, testId.toString());

			Optional<Component> result = converter.fromNbt(nbt);

			assertTrue(result.isPresent());
			assertEquals(testId, result.get().id());
		}

		@Test
		void decodesComponentFromCompoundNbt() {
			NbtCompound innerNbt = new NbtCompound();
			innerNbt.putString("id", testId.toString());

			Codec<Component> compoundCodec = NbtCompound.CODEC.xmap(
					compound -> testComponent,
					comp -> {
						NbtCompound c = new NbtCompound();
						c.putString("id", comp.id().toString());
						return c;
					}
			);
			ComponentNbtConverter compoundConverter = new ComponentNbtConverter(compoundCodec);

			NbtCompound nbt = new NbtCompound();
			nbt.put(ComponentNbtConverter.FORGERO_NBT_KEY, innerNbt);

			Optional<Component> result = compoundConverter.fromNbt(nbt);

			assertTrue(result.isPresent());
			assertEquals(testId, result.get().id());
		}

		@Test
		void returnsEmptyWhenValueIsWrongType() {
			NbtCompound nbt = new NbtCompound();
			nbt.putInt(ComponentNbtConverter.FORGERO_NBT_KEY, 42);

			Optional<Component> result = converter.fromNbt(nbt);

			assertTrue(result.isEmpty());
		}

		@Test
		void nbtContainsMethodDistinguishesStringFromCompound() {
			NbtCompound stringNbt = new NbtCompound();
			stringNbt.putString(ComponentNbtConverter.FORGERO_NBT_KEY, "test:id");

			NbtCompound compoundNbt = new NbtCompound();
			compoundNbt.put(ComponentNbtConverter.FORGERO_NBT_KEY, new NbtCompound());

			assertTrue(stringNbt.contains(ComponentNbtConverter.FORGERO_NBT_KEY, NbtElement.STRING_TYPE));
			assertFalse(stringNbt.contains(ComponentNbtConverter.FORGERO_NBT_KEY, NbtElement.COMPOUND_TYPE));

			assertTrue(compoundNbt.contains(ComponentNbtConverter.FORGERO_NBT_KEY, NbtElement.COMPOUND_TYPE));
			assertFalse(compoundNbt.contains(ComponentNbtConverter.FORGERO_NBT_KEY, NbtElement.STRING_TYPE));
		}
	}

	@Nested
	class ToNbt {

		@Test
		void serializesComponentToNbt() {
			NbtCompound result = converter.toNbt(testComponent);

			assertTrue(result.contains(ComponentNbtConverter.FORGERO_NBT_KEY));
		}

		@Test
		void serializedNbtCanBeDeserialized() {
			NbtCompound nbt = converter.toNbt(testComponent);

			Optional<Component> roundTrip = converter.fromNbt(nbt);

			assertTrue(roundTrip.isPresent());
			assertEquals(testComponent.id(), roundTrip.get().id());
		}
	}

	@Nested
	class RoundTrip {

		@Test
		void stringEncodedComponentRoundTrips() {
			NbtCompound nbt = new NbtCompound();
			nbt.putString(ComponentNbtConverter.FORGERO_NBT_KEY, testId.toString());

			Optional<Component> decoded = converter.fromNbt(nbt);
			assertTrue(decoded.isPresent());

			NbtCompound reEncoded = converter.toNbt(decoded.get());
			Optional<Component> reDecoded = converter.fromNbt(reEncoded);

			assertTrue(reDecoded.isPresent());
			assertEquals(testId, reDecoded.get().id());
		}
	}
}
