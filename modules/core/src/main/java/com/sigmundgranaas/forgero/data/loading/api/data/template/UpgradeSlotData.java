package com.sigmundgranaas.forgero.data.loading.api.data.template;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * DTO for an upgrade slot definition within a part template or tool template.
 *
 * @param id          A unique identifier for this specific slot instance.
 * @param type        The type of upgrade material/component accepted (e.g., "forgero:upgrade_material", "forgero:binding").
 * @param tags        The slot's identity tags, e.g. its context ("forgero:contexts/offensive").
 *                    Matched by {@code in_slot_type} conditions alongside the slot type, so a
 *                    contextual upgrade bonus can gate on the slot's context.
 * @param tier        Optional tier requirement for the upgrade.
 * @param description Optional translatable description key for the slot.
 * @param kind        Optional slot <em>kind</em> ({@code Slot.type()}) selecting which slot
 *                    implementation to build (default {@code forgero:component_upgrade}). A plugin
 *                    can author its own slot kind by registering a {@code SlotFactory} for it.
 */
public record UpgradeSlotData(
		OpenIdentifier id,
		OpenIdentifier type,
		@Nullable
		List<OpenIdentifier> tags,
		@Nullable
		Integer tier,
		@Nullable
		String description,
		@Nullable
		OpenIdentifier kind
) {
	/** Backward-compatible constructor defaulting {@code kind} (the standard component-upgrade slot). */
	public UpgradeSlotData(OpenIdentifier id, OpenIdentifier type, @Nullable List<OpenIdentifier> tags,
	                       @Nullable Integer tier, @Nullable String description) {
		this(id, type, tags, tier, description, null);
	}
}
