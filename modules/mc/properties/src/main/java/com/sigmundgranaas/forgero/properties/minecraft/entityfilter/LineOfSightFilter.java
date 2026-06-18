package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Selects only entities with an unobstructed line of sight from the source (no solid blocks
 * between them). Makes AOE/chain effects respect walls.
 */
public record LineOfSightFilter() implements EntityFilter {
	public static final String TYPE = "forgero:line_of_sight";
	public static final LineOfSightFilter INSTANCE = new LineOfSightFilter();
	public static final Codec<LineOfSightFilter> CODEC = Codec.unit(INSTANCE);

	@Override
	public boolean test(Entity source, Entity candidate) {
		Vec3d start = source.getEyePos();
		Vec3d end = candidate.getEyePos();
		BlockHitResult hit = source.getWorld().raycast(new RaycastContext(
				start, end,
				RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE,
				source));
		// MISS = nothing blocked the ray; otherwise a block sits between source and candidate.
		return hit.getType() == HitResult.Type.MISS;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
