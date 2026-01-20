package com.sigmundgranaas.forgero.common.tooltip.section;

import com.sigmundgranaas.forgero.common.tooltip.api.TooltipSection;
import com.sigmundgranaas.forgero.common.tooltip.section.impl.*;

/**
 * Registers the default Forgero tooltip sections.
 * <p>
 * Call {@link #register()} during mod initialization to set up standard sections.
 *
 * <h2>Default Sections (in order)</h2>
 * <table>
 *   <tr><th>Section</th><th>Priority</th><th>Header</th><th>Purpose</th></tr>
 *   <tr><td>subtitle</td><td>0</td><td>No</td><td>Item subtype/variant</td></tr>
 *   <tr><td>description</td><td>100</td><td>Yes</td><td>Main item description</td></tr>
 *   <tr><td>notes</td><td>200</td><td>Yes</td><td>Usage notes/tips</td></tr>
 *   <tr><td>attributes</td><td>300</td><td>Yes</td><td>Item stats (damage, speed, etc.)</td></tr>
 *   <tr><td>parts</td><td>350</td><td>Yes</td><td>Structural components (blade, handle)</td></tr>
 *   <tr><td>features</td><td>400</td><td>Yes</td><td>Special abilities/features</td></tr>
 *   <tr><td>upgrades</td><td>500</td><td>Yes</td><td>Applied upgrades</td></tr>
 *   <tr><td>slots</td><td>600</td><td>Yes</td><td>Available upgrade slots</td></tr>
 *   <tr><td>lore</td><td>900</td><td>Yes</td><td>Flavor text</td></tr>
 * </table>
 *
 * <h2>Customization</h2>
 * <p>To add a custom section between existing ones, use an appropriate priority:
 * <pre>{@code
 * // Add a "warnings" section between description (100) and notes (200)
 * TooltipApi.registerSection(
 *     TooltipSection.of("mymod:warnings", 150, true),
 *     ctx -> new WarningsSectionWriter(ctx)
 * );
 * }</pre>
 *
 * @see com.sigmundgranaas.forgero.common.tooltip.api.TooltipApi
 */
public final class DefaultSections {

	// Section definitions as public constants for external reference
	public static final TooltipSection SUBTITLE = TooltipSection.forgeroHeaderless("subtitle", 0);
	public static final TooltipSection DESCRIPTION = TooltipSection.forgero("description", 100);
	public static final TooltipSection NOTES = TooltipSection.forgero("notes", 200);
	public static final TooltipSection ATTRIBUTES = TooltipSection.forgero("attributes", 300);
	public static final TooltipSection PARTS = TooltipSection.forgero("parts", 350);
	public static final TooltipSection FEATURES = TooltipSection.forgero("features", 400);
	public static final TooltipSection UPGRADES = TooltipSection.forgero("upgrades", 500);
	public static final TooltipSection SLOTS = TooltipSection.forgero("slots", 600);
	public static final TooltipSection LORE = TooltipSection.forgero("lore", 900);

	private static boolean registered = false;

	private DefaultSections() {
	}

	/**
	 * Registers all default sections with their writers.
	 * <p>
	 * This method is idempotent - calling it multiple times has no effect.
	 */
	public static void register() {
		if (registered) {
			return;
		}

		SectionRegistry.register(SUBTITLE, SubtitleSectionWriter::new);
		SectionRegistry.register(DESCRIPTION, DescriptionSectionWriter::new);
		SectionRegistry.register(NOTES, NotesSectionWriter::new);
		SectionRegistry.register(ATTRIBUTES, AttributeSectionWriter::new);
		SectionRegistry.register(PARTS, PartsSectionWriter::new);
		SectionRegistry.register(FEATURES, FeaturesSectionWriter::new);
		SectionRegistry.register(UPGRADES, UpgradesSectionWriter::new);
		SectionRegistry.register(SLOTS, SlotsSectionWriter::new);
		SectionRegistry.register(LORE, LoreSectionWriter::new);

		registered = true;
	}

	/**
	 * Checks if default sections have been registered.
	 *
	 * @return true if register() has been called
	 */
	public static boolean isRegistered() {
		return registered;
	}

	/**
	 * Resets registration state. For testing only.
	 */
	public static void reset() {
		registered = false;
	}
}
