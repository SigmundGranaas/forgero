package com.sigmundgranaas.forgero.example;

// THIRD-PARTY ADDON — public API validation harness.
// Discipline: this file may import ONLY from these namespaces (what a real downstream mod
// would have on its classpath as "the Forgero API"):
//   - com.sigmundgranaas.forgero.common.api.*          (services + plugin entrypoints)
//   - com.sigmundgranaas.forgero.common.identifier.api (OpenIdentifier)
//   - net.minecraft.*                                  (the game)
//   - java.*                                           (the JDK)
// Any import outside those namespaces is an API LEAK and is called out in the friction report.

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.common.api.item.ItemComparisonApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Set;

/**
 * Scenarios a downstream mod author realistically wants. Each method uses ONLY the public API.
 * Where the public API is insufficient or awkward, the friction is noted in a NOTE comment and
 * summarized in docs/forgero-2-public-api-review.md.
 */
public final class ExampleForgeroAddon {

	// Scenario 1 — read a tool's stats for a custom HUD / compat layer.
	public String describeTool(ItemStack stack) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		if (!query.isForgeroItem(stack)) {
			return "not a forgero item";
		}
		float damage = query.getAttackDamage(stack);
		int durability = query.getMaxDurability(stack);
		float miningSpeed = query.getMiningSpeed(stack);
		int miningLevel = query.getMiningLevel(stack);
		List<ItemStack> upgrades = query.getInstalledUpgrades(stack);
		Set<OpenIdentifier> tags = query.getTags(stack);
		OpenIdentifier material = query.getPrimaryMaterial(stack).orElse(null);

		return String.format("dmg=%.1f dura=%d mine=%.1f lvl=%d upgrades=%d material=%s tags=%d",
				damage, durability, miningSpeed, miningLevel, upgrades.size(), material, tags.size());
	}

	// Scenario 2 — a gameplay feature: auto-socket a gem into the held tool when compatible.
	public ItemStack autoSocket(ItemStack tool, ItemStack gem) {
		ItemMutationApi mutate = ForgeroApi.itemMutation();
		if (mutate.canInstallUpgrade(tool, gem)) {
			return mutate.installUpgrade(tool, gem); // immutable: returns a new stack
		}
		return tool;
	}

	// Scenario 3 — strip a specific upgrade by id (e.g. an "unsocket" station).
	public ItemStack unsocket(ItemStack tool, String upgradeId) {
		return ForgeroApi.itemMutation().removeUpgrade(tool, OpenIdentifier.parse(upgradeId));
	}

	// Scenario 4 — compat/balancing: is this the "same kind" of tool, and which hits harder?
	public ItemStack strongerOf(ItemStack a, ItemStack b) {
		ItemComparisonApi compare = ForgeroApi.itemComparison();
		ItemQueryApi query = ForgeroApi.itemQuery();
		if (compare.isSameType(a, b)) {
			return a; // identical type — caller's choice
		}
		return query.getAttackDamage(a) >= query.getAttackDamage(b) ? a : b;
	}

	// Scenario 5 — react to Forgero being ready and do something with its data.
	public void onModInit() {
		ForgeroInitializedCallback.EVENT.register(services -> {
			// NOTE(leak): services.componentRegistry() exposes core.component.api.Component —
			// there is no ItemStack-level way to enumerate "all loaded materials/parts". A mod
			// that wants to, say, list every material for a JEI-style screen must drop into the
			// internal Component type. The ItemStack APIs cover *a given stack*, not discovery.
			int loaded = services.componentRegistry().all().size();
			System.out.println("[example] Forgero ready with " + loaded + " components");
		});
	}
}
