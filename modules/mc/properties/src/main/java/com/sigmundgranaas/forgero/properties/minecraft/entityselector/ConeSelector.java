package com.sigmundgranaas.forgero.properties.minecraft.entityselector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Selects entities within a cone-shaped area in front of the source entity.
 * Useful for sweep attacks, breath weapons, and other directional AOE effects.
 * <p>
 * The cone is defined by:
 * - angle: The total angle of the cone in degrees (e.g., 90 means 45 degrees on each side)
 * - range: The maximum distance from the source
 */
public class ConeSelector extends FilterableSelector {
	public static final String TYPE = "forgero:cone";

	private final float angle;
	private final float range;

	public static final Codec<ConeSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("angle").forGetter(ConeSelector::angle),
			Codec.FLOAT.fieldOf("range").forGetter(ConeSelector::range),
			Codec.list(EntityFilter.CODEC).optionalFieldOf("filters", Collections.emptyList()).forGetter(FilterableSelector::filters)
	).apply(instance, ConeSelector::new));

	public ConeSelector(float angle, float range, List<EntityFilter> filters) {
		super(filters);
		if (angle <= 0 || angle > 360) {
			throw new IllegalArgumentException("angle must be between 0 and 360 degrees, got: " + angle);
		}
		if (range <= 0) {
			throw new IllegalArgumentException("range must be > 0, got: " + range);
		}
		this.angle = angle;
		this.range = range;
	}

	public float angle() {
		return angle;
	}

	public float range() {
		return range;
	}

	@Override
	protected List<Entity> selectEntities(Entity source, Entity initialTarget) {
		// Get the direction the source is looking/facing
		Vec3d sourcePos = source.getPos();
		Vec3d lookDirection = source.getRotationVector().normalize();

		// Create a box around the source to find potential targets
		Box searchBox = new Box(source.getBlockPos()).expand(range);
		List<Entity> potentialTargets = source.getWorld().getOtherEntities(source, searchBox);

		List<Entity> targetsInCone = new ArrayList<>();

		for (Entity candidate : potentialTargets) {
			Vec3d candidatePos = candidate.getPos();
			Vec3d toCandidate = candidatePos.subtract(sourcePos).normalize();

			// Calculate distance
			double distance = sourcePos.distanceTo(candidatePos);
			if (distance > range) {
				continue;
			}

			// Calculate angle between look direction and direction to candidate
			// Clamp dot product to [-1, 1] to handle floating point errors
			double dotProduct = Math.max(-1.0, Math.min(1.0, lookDirection.dotProduct(toCandidate)));
			double angleToCandidate = Math.toDegrees(Math.acos(dotProduct));

			// Check if entity is within the cone angle
			if (angleToCandidate <= angle / 2.0) {
				targetsInCone.add(candidate);
			}
		}

		return targetsInCone;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
