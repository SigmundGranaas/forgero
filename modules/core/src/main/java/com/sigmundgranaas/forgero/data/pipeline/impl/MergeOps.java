package com.sigmundgranaas.forgero.data.pipeline.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.IdentifierEntry;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

/**
 * Shared field-level merge operations for combining two definitions of the same identity, used by
 * both {@link DefinitionMerger} (same-id definitions across packs) and {@link ExtensionMerger}
 * (extension overlays).
 *
 * <p>Semantics (the {@code add} side is the higher-priority / later contributor):
 * <ul>
 *   <li><b>tags / host identifiers / includes</b>: union (order-preserving, de-duplicated)</li>
 *   <li><b>attributes / upgrade slots</b>: keyed by id — a matching id overrides in place, otherwise
 *       the element is appended</li>
 *   <li><b>properties</b>: deep merge (objects recurse, arrays concatenate, primitives: add wins)</li>
 * </ul>
 */
public final class MergeOps {
	private MergeOps() {
	}

	public static List<OpenIdentifier> union(List<OpenIdentifier> base, List<OpenIdentifier> add) {
		if (add == null || add.isEmpty()) {
			return base;
		}
		if (base == null || base.isEmpty()) {
			return add;
		}
		Set<OpenIdentifier> merged = new LinkedHashSet<>(base);
		merged.addAll(add);
		return new ArrayList<>(merged);
	}

	/**
	 * Merges attribute lists keyed by {@link AttributeData#id()}: an attribute whose id matches one
	 * already present overrides it in place; attributes with no id, or a new id, are appended. Order
	 * of existing entries is preserved.
	 */
	public static List<AttributeData> mergeAttributesById(List<AttributeData> base, List<AttributeData> add) {
		if (add == null || add.isEmpty()) {
			return base;
		}
		if (base == null || base.isEmpty()) {
			return add;
		}
		List<AttributeData> result = new ArrayList<>(base);
		// Index existing attributes by id (first occurrence wins) so later contributors override in place.
		Map<OpenIdentifier, Integer> idIndex = new HashMap<>();
		for (int i = 0; i < result.size(); i++) {
			OpenIdentifier id = result.get(i).id().orElse(null);
			if (id != null) {
				idIndex.putIfAbsent(id, i);
			}
		}
		for (AttributeData attr : add) {
			OpenIdentifier id = attr.id().orElse(null);
			if (id != null && idIndex.containsKey(id)) {
				result.set(idIndex.get(id), attr);
			} else {
				result.add(attr);
				if (id != null) {
					idIndex.put(id, result.size() - 1);
				}
			}
		}
		return result;
	}

	/**
	 * Merges upgrade slots keyed by {@link UpgradeSlotData#id()}: a matching slot id overrides,
	 * otherwise the slot is appended.
	 */
	public static List<UpgradeSlotData> mergeUpgradesById(List<UpgradeSlotData> base, List<UpgradeSlotData> add) {
		if (add == null || add.isEmpty()) {
			return base;
		}
		if (base == null || base.isEmpty()) {
			return add;
		}
		Map<OpenIdentifier, UpgradeSlotData> slotMap = new java.util.LinkedHashMap<>();
		for (UpgradeSlotData slot : base) {
			slotMap.put(slot.id(), slot);
		}
		for (UpgradeSlotData slot : add) {
			slotMap.put(slot.id(), slot);
		}
		return new ArrayList<>(slotMap.values());
	}

	/**
	 * Unions two host declarations: identifier lists are concatenated (de-duplicated), and the first
	 * non-null {@code create} spec wins.
	 */
	public static HostData mergeHost(HostData base, HostData add) {
		if (add == null) {
			return base;
		}
		if (base == null) {
			return add;
		}
		Set<IdentifierEntry> ids = new LinkedHashSet<>();
		if (base.identifiers() != null) {
			ids.addAll(base.identifiers());
		}
		if (add.identifiers() != null) {
			ids.addAll(add.identifiers());
		}
		return new HostData(ids.isEmpty() ? null : new ArrayList<>(ids),
				base.create() != null ? base.create() : add.create());
	}

	public static Map<String, JsonElement> mergeProperties(Map<String, JsonElement> base, Map<String, JsonElement> add) {
		if (add == null || add.isEmpty()) {
			return base;
		}
		if (base == null || base.isEmpty()) {
			return add;
		}
		Map<String, JsonElement> merged = new HashMap<>(base);
		for (Map.Entry<String, JsonElement> entry : add.entrySet()) {
			JsonElement existing = merged.get(entry.getKey());
			merged.put(entry.getKey(), existing == null ? entry.getValue() : deepMergeJson(existing, entry.getValue()));
		}
		return merged;
	}

	public static JsonElement deepMergeJson(JsonElement base, JsonElement add) {
		if (base.isJsonArray() && add.isJsonArray()) {
			JsonArray merged = new JsonArray();
			base.getAsJsonArray().forEach(merged::add);
			add.getAsJsonArray().forEach(merged::add);
			return merged;
		}
		if (base.isJsonObject() && add.isJsonObject()) {
			JsonObject merged = new JsonObject();
			base.getAsJsonObject().entrySet().forEach(e -> merged.add(e.getKey(), e.getValue()));
			for (Map.Entry<String, JsonElement> e : add.getAsJsonObject().entrySet()) {
				merged.add(e.getKey(), merged.has(e.getKey()) ? deepMergeJson(merged.get(e.getKey()), e.getValue()) : e.getValue());
			}
			return merged;
		}
		return add;
	}
}
