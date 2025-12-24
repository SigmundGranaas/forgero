package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

/**
 * Plays a sound effect when hand is swung.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:swing_sound",
 *   "sound": "minecraft:entity.player.attack.sweep",
 *   "volume": 1.0,
 *   "pitch": 1.0
 * }
 * </pre>
 */
public record SwingSoundEffect(
		String sound,
		float volume,
		float pitch
) implements SwingEffect {
	public static final String TYPE = "forgero:swing_sound";

	public static final Codec<SwingSoundEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("sound").forGetter(SwingSoundEffect::sound),
			Codec.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(SwingSoundEffect::volume),
			Codec.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(SwingSoundEffect::pitch)
	).apply(instance, SwingSoundEffect::new));

	@Override
	public void apply(Entity source, Hand hand) {
		if (source.getWorld().isClient()) {
			return;
		}

		Identifier soundId = new Identifier(sound);
		SoundEvent soundEvent = Registries.SOUND_EVENT.get(soundId);

		if (soundEvent != null) {
			source.getWorld().playSound(
					null,
					source.getBlockPos(),
					soundEvent,
					SoundCategory.PLAYERS,
					volume,
					pitch
			);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
