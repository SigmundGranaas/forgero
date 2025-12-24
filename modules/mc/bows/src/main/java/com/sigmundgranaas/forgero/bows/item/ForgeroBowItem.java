package com.sigmundgranaas.forgero.bows.item;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.core.component.api.Component;

import net.minecraft.item.BowItem;

/**
 * A Forgero-specific bow item. It extends the vanilla BowItem but is also a
 * ForgeroHostItem, holding a direct reference to its default Forgero component.
 * This allows the item to be a bridge between the vanilla item system and Forgero's component system.
 *
 * <p>Unlike the legacy DynamicBowItem, this class does not handle use actions directly.
 * Instead, all use interaction behavior is defined via UseInteractionProperty in JSON
 * and handled by the UseInteractionManager mixins.</p>
 */
public class ForgeroBowItem extends BowItem implements ForgeroHostItem {
	private final Component component;

	public ForgeroBowItem(Settings settings, Component component) {
		super(settings);
		this.component = component;
	}

	@Override
	public Component getForgeroComponent() {
		return component;
	}
}
