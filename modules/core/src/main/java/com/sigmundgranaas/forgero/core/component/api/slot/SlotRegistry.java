package com.sigmundgranaas.forgero.core.component.api.slot;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry for mutable Slot codecs.
 * Enables polymorphic (de)serialization of different slot types.
 *
 * Plugins can register custom slot types (ArrowSlot, SoulSlot, etc.) via DataPlugin.
 */
public final class SlotRegistry {
	private static final Map<String, Codec<? extends Slot>> SLOT_CODECS = new ConcurrentHashMap<>();

	private SlotRegistry() {}

	/**
	 * Registers a slot codec for the given type identifier.
	 *
	 * @param type The slot type identifier (e.g., "forgero:component_upgrade")
	 * @param codec The codec for (de)serializing this slot type
	 */
	public static void register(String type, Codec<? extends Slot> codec) {
		if (SLOT_CODECS.containsKey(type)) {
			throw new IllegalArgumentException("Slot type already registered: " + type);
		}
		SLOT_CODECS.put(type, codec);
	}

	/**
	 * Gets the codec for a given slot type.
	 *
	 * @param type The slot type identifier
	 * @return The codec for this slot type
	 * @throws IllegalArgumentException if the type is not registered
	 */
	public static Codec<? extends Slot> getCodec(String type) {
		Codec<? extends Slot> codec = SLOT_CODECS.get(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown slot type: " + type + ". Available types: " + SLOT_CODECS.keySet());
		}
		return codec;
	}

	/**
	 * Checks if a slot type is registered.
	 *
	 * @param type The slot type identifier
	 * @return true if the type is registered
	 */
	public static boolean isRegistered(String type) {
		return SLOT_CODECS.containsKey(type);
	}

	/**
	 * Polymorphic codec for any Slot implementation.
	 * Uses the "type" field to dispatch to the appropriate codec.
	 */
	public static final Codec<Slot> CODEC = DispatchCodecUtils.create(
		SlotRegistry::getCodec,
		slot -> slot.type().toString()
	);
}
