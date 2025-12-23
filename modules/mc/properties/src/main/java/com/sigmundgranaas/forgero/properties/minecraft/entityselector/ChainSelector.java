package com.sigmundgranaas.forgero.properties.minecraft.entityselector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Selects entities by chaining from the initial target to nearby entities.
 * Perfect for chain lightning or bouncing projectile effects.
 * <p>
 * The chain works by:
 * 1. Starting with the initial target
 * 2. Finding the nearest entity within chainRange
 * 3. Repeating until maxChains is reached or no more entities are found
 * <p>
 * Parameters:
 * - maxChains: Maximum number of times to chain (total targets = maxChains + 1 including initial)
 * - chainRange: Maximum distance to the next entity in the chain
 * - allowRepeats: Whether the chain can hit the same entity multiple times
 */
public class ChainSelector extends FilterableSelector {
	public static final String TYPE = "forgero:chain";

	private final int maxChains;
	private final float chainRange;
	private final boolean allowRepeats;

	public static final Codec<ChainSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("maxChains").forGetter(ChainSelector::maxChains),
			Codec.FLOAT.fieldOf("chainRange").forGetter(ChainSelector::chainRange),
			Codec.BOOL.optionalFieldOf("allowRepeats", false).forGetter(ChainSelector::allowRepeats),
			Codec.list(EntityFilter.CODEC).optionalFieldOf("filters", Collections.emptyList()).forGetter(FilterableSelector::filters)
	).apply(instance, ChainSelector::new));

	public ChainSelector(int maxChains, float chainRange, boolean allowRepeats, List<EntityFilter> filters) {
		super(filters);
		if (maxChains < 0) {
			throw new IllegalArgumentException("maxChains must be >= 0, got: " + maxChains);
		}
		if (chainRange <= 0) {
			throw new IllegalArgumentException("chainRange must be > 0, got: " + chainRange);
		}
		this.maxChains = maxChains;
		this.chainRange = chainRange;
		this.allowRepeats = allowRepeats;
	}

	public int maxChains() {
		return maxChains;
	}

	public float chainRange() {
		return chainRange;
	}

	public boolean allowRepeats() {
		return allowRepeats;
	}

	@Override
	protected List<Entity> selectEntities(Entity source, Entity initialTarget) {
		List<Entity> chainedTargets = new ArrayList<>();
		Set<Entity> hitEntities = new HashSet<>();

		// Start with the initial target
		Entity currentTarget = initialTarget;
		chainedTargets.add(currentTarget);
		if (!allowRepeats) {
			hitEntities.add(currentTarget);
		}

		// Chain to nearby entities
		for (int i = 0; i < maxChains; i++) {
			Entity nextTarget = findNearestEntity(source, currentTarget, hitEntities);

			if (nextTarget == null) {
				break; // No more entities to chain to
			}

			chainedTargets.add(nextTarget);
			if (!allowRepeats) {
				hitEntities.add(nextTarget);
			}
			currentTarget = nextTarget;
		}

		return chainedTargets;
	}

	/**
	 * Finds the nearest entity to the current target within chainRange.
	 *
	 * @param source       The original source entity (excluded from selection)
	 * @param currentTarget The current target in the chain
	 * @param hitEntities   Set of entities already hit (if allowRepeats is false)
	 * @return The nearest valid entity, or null if none found
	 */
	private Entity findNearestEntity(Entity source, Entity currentTarget, Set<Entity> hitEntities) {
		Box searchBox = new Box(currentTarget.getBlockPos()).expand(chainRange);
		List<Entity> nearbyEntities = currentTarget.getWorld().getOtherEntities(source, searchBox);

		Entity nearest = null;
		double nearestDistance = Double.MAX_VALUE;

		for (Entity candidate : nearbyEntities) {
			// Skip if already hit (when allowRepeats is false)
			if (!allowRepeats && hitEntities.contains(candidate)) {
				continue;
			}

			double distance = currentTarget.distanceTo(candidate);

			// Check if within range and closer than current nearest
			if (distance <= chainRange && distance < nearestDistance) {
				nearest = candidate;
				nearestDistance = distance;
			}
		}

		return nearest;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
