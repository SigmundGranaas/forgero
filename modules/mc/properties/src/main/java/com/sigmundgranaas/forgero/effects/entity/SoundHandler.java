package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Plays a sound effect at either the source or target entity's location.
 * Provides auditory feedback for weapon effects and abilities.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:sound",
 *   "sound": "minecraft:entity.lightning_bolt.thunder",
 *   "volume": 1.5,
 *   "pitch": 0.8,
 *   "target": "source"
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Impact sounds - play sound on hit</li>
 *   <li>Ability activation - play sound when effect triggers</li>
 *   <li>Feedback - auditory cues for successful actions</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * This handler is lightweight. Server-side only to prevent client desync.
 *
 * @param sound The identifier of the sound to play (e.g., "minecraft:entity.blaze.shoot")
 * @param volume The volume multiplier (1.0 is normal volume)
 * @param pitch The pitch multiplier (1.0 is normal pitch, higher = higher pitch)
 * @param target Whether to play the sound at SOURCE or TARGET location
 */
public record SoundHandler(Identifier sound, float volume, float pitch, SoundTarget target) implements ContextualEffectHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SoundHandler.class);
	public static final String TYPE = "forgero:sound";
	public static final Codec<SoundHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("sound").forGetter(SoundHandler::sound),
			Codec.floatRange(0.0f, 10.0f).optionalFieldOf("volume", 1.0f).forGetter(SoundHandler::volume),
			Codec.floatRange(0.5f, 2.0f).optionalFieldOf("pitch", 1.0f).forGetter(SoundHandler::pitch),
			SoundTarget.CODEC.optionalFieldOf("target", SoundTarget.TARGET).forGetter(SoundHandler::target)
	).apply(instance, SoundHandler::new));

	// Compact constructor for validation
	public SoundHandler {
		if (volume > 5.0f) {
			LOGGER.warn("Large sound volume ({}). May be too loud. Recommended: <= 2.0", volume);
		}
	}

	@Override
	public void apply(Entity source, Entity targetEntity) {
		if (source.getWorld().isClient) {
			return; // Server-side only
		}

		Entity soundLocation = (target == SoundTarget.SOURCE) ? source : targetEntity;
		SoundEvent soundEvent = Registries.SOUND_EVENT.get(sound);

		if (soundEvent == null) {
			LOGGER.debug("Invalid sound identifier in effect: {}", sound);
			return;
		}

		soundLocation.getWorld().playSound(
				null, // null = everyone hears it
				soundLocation.getX(),
				soundLocation.getY(),
				soundLocation.getZ(),
				soundEvent,
				soundLocation.getSoundCategory(),
				volume,
				pitch
		);
	}

	@Override
	public String type() {
		return TYPE;
	}

	/**
	 * Determines where the sound should be played.
	 */
	public enum SoundTarget implements StringIdentifiable {
		/** Play sound at the source entity (attacker) */
		SOURCE,
		/** Play sound at the target entity (victim) */
		TARGET;

		public static final Codec<SoundTarget> CODEC = StringIdentifiable.createCodec(
				SoundTarget::values,
				(value) -> String.valueOf(SoundTarget.valueOf(value.toUpperCase(Locale.ROOT)))
		);

		@Override
		public String asString() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}
}
