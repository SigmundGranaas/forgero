package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Plays a sound effect when a block is hit.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:block_sound",
 *   "sound": "minecraft:block.stone.break",
 *   "volume": 1.0,
 *   "pitch": 1.0
 * }
 * </pre>
 */
public record BlockSoundEffect(
		String sound,
		float volume,
		float pitch
) implements OnHitBlockEffect {
	public static final String TYPE = "forgero:block_sound";

	public static final Codec<BlockSoundEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("sound").forGetter(BlockSoundEffect::sound),
			Codec.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(BlockSoundEffect::volume),
			Codec.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(BlockSoundEffect::pitch)
	).apply(instance, BlockSoundEffect::new));

	@Override
	public void apply(World world, Entity source, BlockPos pos) {
		if (world.isClient()) {
			return;
		}

		Identifier soundId = new Identifier(sound);
		SoundEvent soundEvent = Registries.SOUND_EVENT.get(soundId);

		if (soundEvent != null) {
			world.playSound(
					null,
					pos,
					soundEvent,
					SoundCategory.BLOCKS,
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
