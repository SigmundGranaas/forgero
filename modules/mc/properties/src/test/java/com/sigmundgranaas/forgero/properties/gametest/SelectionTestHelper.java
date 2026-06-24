package com.sigmundgranaas.forgero.properties.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.AreaOfEffectSelector;

import net.minecraft.entity.Entity;

/**
 * Harness for proving an {@link EntityFilter} actually narrows a real selection, instead of only
 * calling {@code filter.test(fabricatedEntity)} in isolation (the gap the audit flagged across the
 * ~30 filter tests).
 * <p>
 * It runs the real {@link AreaOfEffectSelector} — the same selector the on-hit/AOE effects use — with
 * {@code filter} over the entities around {@code initialTarget}, and returns exactly which entities it
 * selects. A test then asserts a matching entity IS selected and a non-matching one is SPARED, both in
 * one real selection. Assert specific membership (contains match / does-not-contain non-match) rather
 * than an exact count, so the test stays robust to other entities sharing the gametest world.
 */
public final class SelectionTestHelper {

	private SelectionTestHelper() {
	}

	/**
	 * Runs a real AOE selection of {@code radius} around {@code initialTarget} with {@code filter}
	 * applied (source excluded, as in combat) and returns the selected entities.
	 */
	public static List<Entity> selectWithFilter(Entity source, Entity initialTarget, EntityFilter filter, int radius) {
		return new AreaOfEffectSelector(radius, List.of(filter)).select(source, initialTarget);
	}
}
