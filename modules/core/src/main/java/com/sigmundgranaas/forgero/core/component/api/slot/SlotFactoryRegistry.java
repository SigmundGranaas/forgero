package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Global registry mapping a slot <em>kind</em> ({@code Slot.type()}) to the {@link SlotFactory} that
 * materialises it from data.
 * <p>
 * This is the load-time dispatch that lets new slot kinds be authored in part templates: a template
 * upgrade entry may carry a {@code "kind"} field, and the component builder uses this registry to
 * build the right {@link Slot} instead of hardcoding {@link ComponentUpgradeSlot}. Plugins register
 * their kinds via the registration context.
 * <p>
 * {@link #DEFAULT_KIND} ({@code forgero:component_upgrade}) is always registered, so existing content
 * with no {@code kind} field is unchanged.
 */
public final class SlotFactoryRegistry {

	/** The default kind, used when a slot definition omits {@code kind}. */
	public static final OpenIdentifier DEFAULT_KIND = OpenIdentifier.parse(ComponentUpgradeSlot.TYPE);

	private static final Map<OpenIdentifier, SlotFactory> FACTORIES = new ConcurrentHashMap<>();

	static {
		FACTORIES.put(DEFAULT_KIND, SlotFactoryRegistry::buildComponentUpgrade);
	}

	private SlotFactoryRegistry() {
	}

	/**
	 * Registers a factory for the given slot kind.
	 *
	 * @throws IllegalArgumentException if the kind is already registered
	 */
	public static void register(OpenIdentifier kind, SlotFactory factory) {
		if (FACTORIES.putIfAbsent(kind, factory) != null) {
			throw new IllegalArgumentException("Slot kind already registered: " + kind);
		}
	}

	/**
	 * @param kind the slot kind, or null for {@link #DEFAULT_KIND}
	 * @return the factory for this kind, or null if the kind is unknown (callers should fail loudly)
	 */
	public static SlotFactory get(OpenIdentifier kind) {
		return FACTORIES.get(kind == null ? DEFAULT_KIND : kind);
	}

	public static boolean isRegistered(OpenIdentifier kind) {
		return FACTORIES.containsKey(kind == null ? DEFAULT_KIND : kind);
	}

	/** The default factory: a tag-validated {@link ComponentUpgradeSlot}. */
	private static Slot buildComponentUpgrade(SlotFactory.SlotSpec spec, Optional<Component> content) {
		SlotValidator validator;
		if (spec.validTags() != null && !spec.validTags().isEmpty()) {
			validator = SlotValidator.requireAllTags(spec.validTags());
		} else if (spec.slotType() != null) {
			validator = SlotValidator.requireTag(spec.slotType());
		} else {
			validator = SlotValidator.ACCEPT_ALL;
		}
		return new ComponentUpgradeSlot(spec.id(), spec.slotType(), spec.description(),
				spec.tags() == null ? java.util.Set.of() : spec.tags(), validator, content);
	}
}
