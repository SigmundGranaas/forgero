package com.sigmundgranaas.forgero.common.api.item.impl;

import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import net.minecraft.item.ItemStack;

/**
 * Package-private implementation of {@link ItemMutationApi}.
 * <p>
 * This implementation delegates to existing Forgero services (ComponentConverter, SlotManager)
 * and handles ItemStack round-tripping for mutations.
 */
public class ItemMutationApiImpl implements ItemMutationApi {

	private final ComponentConverter converter;
	private final SlotManager slotManager;

	/**
	 * Creates a new ItemMutationApiImpl instance.
	 *
	 * @param converter   The component converter
	 * @param slotManager The slot manager
	 */
	public ItemMutationApiImpl(ComponentConverter converter, SlotManager slotManager) {
		this.converter = converter;
		this.slotManager = slotManager;
	}

	@Override
	public ItemStack installUpgrade(ItemStack target, ItemStack upgrade) {
		if (target == null || target.isEmpty() || upgrade == null || upgrade.isEmpty()) {
			return target;
		}

		var targetComp = converter.toComponent(target);
		var upgradeComp = converter.toComponent(upgrade);

		if (targetComp.isEmpty() || upgradeComp.isEmpty()) {
			return target;
		}

		// Use SlotManager to install the upgrade
		var installResult = slotManager.install(targetComp.get(), upgradeComp.get());

		if (installResult.success() && installResult.component().isPresent()) {
			// Convert back to ItemStack
			return converter.toStack(installResult.component().get())
				.orElse(target);
		}

		return target; // Return original on failure
	}

	@Override
	public ItemStack removeUpgrade(ItemStack target, OpenIdentifier upgradeId) {
		if (target == null || target.isEmpty() || upgradeId == null) {
			return target;
		}

		return converter.toComponent(target)
			.map(component -> {
				// Use SlotManager to remove upgrade by ID
				var updated = slotManager.removeUpgrade(component, upgradeId);
				return converter.toStack(updated).orElse(target);
			})
			.orElse(target);
	}

	@Override
	public ItemStack removeAllUpgrades(ItemStack target) {
		if (target == null || target.isEmpty()) {
			return target;
		}

		return converter.toComponent(target)
			.map(component -> {
				// Use SlotManager to remove all upgrades
				var updated = slotManager.removeAllUpgrades(component);
				return converter.toStack(updated).orElse(target);
			})
			.orElse(target);
	}

	@Override
	public boolean canInstallUpgrade(ItemStack target, ItemStack upgrade) {
		if (target == null || target.isEmpty() || upgrade == null || upgrade.isEmpty()) {
			return false;
		}

		var targetComp = converter.toComponent(target);
		var upgradeComp = converter.toComponent(upgrade);

		if (targetComp.isEmpty() || upgradeComp.isEmpty()) {
			return false;
		}

		// Use SlotManager to check compatibility
		return slotManager.canInstall(targetComp.get(), upgradeComp.get());
	}
}
