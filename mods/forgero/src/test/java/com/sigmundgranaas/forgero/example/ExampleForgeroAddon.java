package com.sigmundgranaas.forgero.example;

// THIRD-PARTY ADDON — public API validation harness (and the worked example for
// docs/guides/modding-api.md). Discipline: this file may import ONLY from these namespaces (what a
// real downstream mod would have on its classpath as "the Forgero API"):
//   - com.sigmundgranaas.forgero.common.api.*          (services + plugin entrypoints)
//   - com.sigmundgranaas.forgero.common.identifier.api (OpenIdentifier)
//   - com.sigmundgranaas.forgero.effects.api.*         (custom effects)
//   - net.minecraft.*                                  (the game)
//   - java.*                                           (the JDK)
// Any import outside those namespaces is an API LEAK. A leak-audit asserts this stays clean.

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.common.api.item.ItemComparisonApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.effects.api.BlockEffects;
import com.sigmundgranaas.forgero.effects.api.OnHitEffects;
import com.sigmundgranaas.forgero.effects.api.UseEffects;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

	// Scenario 5 — registry-wide discovery for a JEI-style screen, without touching Component.
	public List<ItemStack> allForgeroMaterials() {
		return ForgeroApi.itemQuery().allMaterials();
	}

	public List<ItemStack> metalsOnly() {
		return ForgeroApi.itemQuery().findByTag(OpenIdentifier.parse("forgero:materials/types/metal"));
	}

	// Scenario 7 — register custom on-hit effects usable from content JSON, with plain Minecraft
	// logic and no internal types (public effects.api.* surface).
	public void registerEffects() {
		// No config — ignite the victim.
		OnHitEffects.registerSingleTarget("example:torch", entity -> entity.setOnFireFor(3));

		// Source + target — heal the attacker (lifesteal), no config.
		OnHitEffects.registerSourceTarget("example:lifesteal", (source, target) -> {
			if (source instanceof LivingEntity attacker) {
				attacker.heal(2.0f);
			}
		});

		// Block channel — turn the struck block to magma.
		BlockEffects.register("example:scorch",
				(world, source, pos) -> world.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState()));

		// Use channel — warm the user on right-click.
		UseEffects.register("example:warm", (user, stack, hand) -> user.setOnFireFor(1));
	}

	// Scenario 8 — react to Forgero being ready. For content discovery prefer the ItemStack APIs
	// (allMaterials/allParts/findByTag) over services.componentRegistry(), which exposes the
	// internal Component type.
	public void onModInit() {
		registerEffects();
		ForgeroInitializedCallback.EVENT.register(services ->
				System.out.println("[example] Forgero ready; " + allForgeroMaterials().size() + " materials available"));
	}
}
