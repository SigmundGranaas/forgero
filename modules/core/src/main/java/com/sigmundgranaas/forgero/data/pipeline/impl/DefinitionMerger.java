package com.sigmundgranaas.forgero.data.pipeline.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merges multiple resource definitions that declare the SAME identity (id), so that two independent
 * content packs can each fully define the same resource and have them composed when both are loaded
 * — no shared "base" pack required.
 *
 * <p>Merge order: ascending {@code priority} (ties keep load order); the higher-priority definition
 * is applied last, so it wins on id-keyed overrides. Field semantics are defined in {@link MergeOps}.
 *
 * <p>Only {@link ResourceData} definitions (materials, shapes, schematics, casts, static parts) are
 * structurally merged. A group that mixes in non-mergeable types (templates) keeps the
 * highest-priority entry. Definitions sharing an id but declaring different {@code type}s are an
 * identity clash and fail fast.
 */
public class DefinitionMerger {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionMerger.class);

	/**
	 * Merges all definitions in a same-id group into one.
	 *
	 * @param id   the shared identity
	 * @param defs the definitions sharing that identity, in load order
	 * @return the single merged definition (or the sole definition if the group has one entry)
	 */
	public RawDefinition mergeGroup(OpenIdentifier id, List<RawDefinition> defs) {
		if (defs.size() == 1) {
			return defs.get(0);
		}

		List<RawDefinition> sorted = new ArrayList<>(defs);
		// Stable sort keeps load order among equal priorities; higher priority ends up last.
		sorted.sort(Comparator.comparingInt(RawDefinition::priority));

		boolean allResource = sorted.stream().allMatch(d -> d.data() instanceof ResourceData);
		if (!allResource) {
			RawDefinition winner = sorted.get(sorted.size() - 1);
			LOGGER.warn("Duplicate definition id [{}] includes non-mergeable (template) types; "
					+ "keeping the highest-priority entry.", id);
			return winner;
		}

		ResourceData merged = (ResourceData) sorted.get(0).data();
		for (int i = 1; i < sorted.size(); i++) {
			merged = mergeTwo(id, merged, (ResourceData) sorted.get(i).data());
		}
		LOGGER.debug("Merged {} definitions sharing id [{}]", sorted.size(), id);
		return new RawDefinition(id, merged, sorted.get(sorted.size() - 1).priority());
	}

	private ResourceData mergeTwo(OpenIdentifier id, ResourceData base, ResourceData add) {
		if (!base.type().equals(add.type())) {
			throw new IllegalStateException("Cannot merge definitions sharing id [" + id
					+ "]: conflicting types " + base.type() + " vs " + add.type());
		}
		return new ResourceData(
				base.type(),
				base.name(),
				MergeOps.union(base.include(), add.include()),
				MergeOps.union(base.tags(), add.tags()),
				MergeOps.union(base.localTags(), add.localTags()),
				MergeOps.mergeHost(base.host(), add.host()),
				MergeOps.mergeAttributesById(base.attributes(), add.attributes()),
				MergeOps.mergeAttributesById(base.localAttributes(), add.localAttributes()),
				MergeOps.mergeProperties(base.properties(), add.properties()),
				MergeOps.mergeUpgradesById(base.upgrades(), add.upgrades()),
				base.target() != null ? base.target() : add.target()
		);
	}
}
