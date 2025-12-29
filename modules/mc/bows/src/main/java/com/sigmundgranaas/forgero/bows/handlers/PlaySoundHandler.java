package com.sigmundgranaas.forgero.bows.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.ContextualUseHandler;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Handler that plays a sound effect when the bow is used.
 *
 * <p>Supports pitch variation based on pull progress to match vanilla bow behavior,
 * where a fully drawn bow has a higher pitch than a partially drawn one.
 *
 * <p><b>Example JSON:</b>
 * <pre>{@code
 * {
 *   "type": "forgero:play_sound",
 *   "sound_id": "minecraft:entity.arrow.shoot",
 *   "category": "PLAYERS",
 *   "volume": 1.0,
 *   "base_pitch": 1.0,
 *   "vary_pitch_by_charge": true
 * }
 * }</pre>
 */
public record PlaySoundHandler(
		String soundId,
		String category,
		float volume,
		float basePitch,
		boolean varyPitchByCharge
) implements ContextualUseHandler {
	public static final String TYPE = "forgero:play_sound";

	public static final Codec<PlaySoundHandler> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("sound_id").forGetter(PlaySoundHandler::soundId),
					Codec.STRING.optionalFieldOf("category", "PLAYERS").forGetter(PlaySoundHandler::category),
					Codec.FLOAT.optionalFieldOf("volume", 1.0F).forGetter(PlaySoundHandler::volume),
					Codec.FLOAT.optionalFieldOf("base_pitch", 1.0F).forGetter(PlaySoundHandler::basePitch),
					Codec.BOOL.optionalFieldOf("vary_pitch_by_charge", true).forGetter(PlaySoundHandler::varyPitchByCharge)
			).apply(instance, PlaySoundHandler::new)
	);

	@Override
	public void apply(UseContext context) {
		// Parse sound identifier
		Identifier soundIdentifier = new Identifier(soundId);
		SoundEvent sound = Registries.SOUND_EVENT.get(soundIdentifier);

		if (sound == null) {
			return; // Invalid sound ID, fail silently
		}

		// Parse sound category
		SoundCategory soundCategory;
		try {
			soundCategory = SoundCategory.valueOf(category.toUpperCase());
		} catch (IllegalArgumentException e) {
			soundCategory = SoundCategory.PLAYERS; // Default fallback
		}

		// Calculate pitch with variation based on charge time
		float pitch = basePitch;
		if (varyPitchByCharge) {
			// Vanilla formula: 1.0F / (random * 0.4F + 1.2F) + pullProgress * 0.5F
			// This creates pitch range from ~0.625 to ~1.625
			float randomFactor = 1.0F / (context.world().getRandom().nextFloat() * 0.4F + 1.2F);
			pitch = basePitch * (randomFactor + context.pullProgress() * 0.5F);
		}

		// Play sound at user's position
		context.world().playSound(
				null, // No specific player (plays for all nearby)
				context.user().getX(),
				context.user().getY(),
				context.user().getZ(),
				sound,
				soundCategory,
				volume,
				pitch
		);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
