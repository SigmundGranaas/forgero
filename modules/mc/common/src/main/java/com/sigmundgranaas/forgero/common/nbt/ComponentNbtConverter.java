package com.sigmundgranaas.forgero.common.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ComponentNbtConverter {
	public static final String FORGERO_NBT_KEY = "ForgeroComponent";

	private final Codec<Component> componentCodec;

	/**
	 * Constructs a new converter with a specific codec for Components.
	 * This class should be instantiated once and shared.
	 *
	 * @param componentCodec The codec to use for serialization and deserialization.
	 */
	public ComponentNbtConverter(Codec<Component> componentCodec) {
		this.componentCodec = componentCodec;
	}

	/**
	 * Deserializes a Component from a given NBT compound tag.
	 * It looks for a specific key ({@link #FORGERO_NBT_KEY}) within the compound tag.
	 *
	 * @param nbt The NBT compound to read from. Can be null.
	 * @return An Optional containing the deserialized Component, or empty if the key is not present or deserialization fails.
	 */
	public Optional<Component> fromNbt(NbtCompound nbt) {
		if (nbt == null) {
			return Optional.empty();
		}

		boolean hasCompound = nbt.contains(FORGERO_NBT_KEY, NbtElement.COMPOUND_TYPE);
		boolean hasString = nbt.contains(FORGERO_NBT_KEY, NbtElement.STRING_TYPE);

		if (!hasCompound && !hasString) {
			return Optional.empty();
		}

		NbtElement forgeroNbt = nbt.get(FORGERO_NBT_KEY);

		var logger = LoggerFactory.getLogger(ComponentNbtConverter.class);
		logger.debug("Attempting to decode component from NBT: {}", forgeroNbt);

		return componentCodec.decode(NbtOps.INSTANCE, forgeroNbt)
				.resultOrPartial(err -> {
					logger.error("Failed to decode component from NBT: {}", err);
					logger.debug("NBT data that failed to decode: {}", forgeroNbt);
				})
				.map(Pair::getFirst);
	}

	/**
	 * Serializes a Component into an NBT compound tag.
	 * The resulting NBT will contain a single key ({@link #FORGERO_NBT_KEY}) with the component data.
	 *
	 * @param component The component to serialize.
	 * @return A new NBT compound containing the serialized component data.
	 */
	public NbtCompound toNbt(Component component) {
		NbtCompound nbt = new NbtCompound();
		componentCodec.encodeStart(NbtOps.INSTANCE, component)
				.resultOrPartial(err -> LoggerFactory.getLogger(ComponentNbtConverter.class).error("Failed to encode component to NBT: {}", err))
				.ifPresent(forgeroNbt -> nbt.put(FORGERO_NBT_KEY, forgeroNbt));
		return nbt;
	}
}
