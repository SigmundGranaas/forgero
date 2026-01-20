package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Creates an explosion at the hit block position.
 * Useful for explosive mining effects or combat abilities that detonate on block contact.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:block_explosion",
 *   "power": 3.0,
 *   "create_fire": false
 * }
 * </pre>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>power - Explosion power (TNT is 4.0, creeper is 3.0)</li>
 *   <li>create_fire - Whether the explosion should create fire (default: false)</li>
 * </ul>
 *
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>Creates explosion centered on the hit block</li>
 *   <li>Only triggers server-side to avoid duplicate explosions</li>
 *   <li>Uses MOB explosion source type (no player attribution for griefing)</li>
 * </ul>
 */
public record BlockExplosionEffect(
		float power,
		boolean createFire
) implements OnHitBlockEffect {
	public static final String TYPE = "forgero:block_explosion";

	public static final Codec<BlockExplosionEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("power").forGetter(BlockExplosionEffect::power),
			Codec.BOOL.optionalFieldOf("create_fire", false).forGetter(BlockExplosionEffect::createFire)
	).apply(instance, BlockExplosionEffect::new));

	@Override
	public void apply(World world, Entity source, BlockPos pos) {
		if (world.isClient()) {
			return;
		}
		world.createExplosion(
				source,
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				power,
				createFire,
				World.ExplosionSourceType.MOB
		);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
