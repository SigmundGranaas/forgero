package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.InstallationResult;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for installing iron as a reinforcement upgrade on pickaxe heads.
 * These tests validate the full upgrade installation flow and attribute resolution.
 */
public class IronReinforcementInstallationTest implements ForgeroGameTest {

	private static final OpenIdentifier OFFENSIVE_CONTEXT = OpenIdentifier.parse("forgero:contexts/offensive");

	private final AttributeEngine attributeEngine = new AttributeEngine();

	/**
	 * Gets the raw attribute value directly from component properties.
	 * This does NOT include resolved attributes from upgrades.
	 */
	private float getRawAttributeValue(Component component, String attributeType) {
		return component.properties(Attribute.KEY).stream()
				.filter(attr -> attr.type().toString().equals(attributeType))
				.findFirst()
				.map(Attribute::value)
				.orElse(0f);
	}

	/**
	 * Gets the resolved attribute value using the AttributeEngine.
	 * This INCLUDES attributes from installed upgrades.
	 */
	private float getResolvedAttributeValue(Component component, OpenIdentifier attributeType) {
		AttributeQueryResult result = AttributeEngine.resolveAttributes(component);
		return result.getValue(attributeType);
	}

	private List<Attribute> getAttributes(Component component, String attributeType) {
		return component.properties(Attribute.KEY).stream()
				.filter(attr -> attr.type().toString().equals(attributeType))
				.toList();
	}

	// ========== Iron Material Attribute Tests ==========

	/**
	 * Tests that iron has upgrade-context attributes from metal_upgrade_base.
	 * These attributes should only apply when iron is used as an upgrade.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_has_upgrade_context_attributes(TestContext context) {
		var iron = ForgeroTestUtils.forgero(context).component("forgero:iron").orElseThrow();

		// Get all durability attributes
		List<Attribute> durabilityAttrs = getAttributes(iron, "forgero:durability");

		// Iron should have durability attributes - some with part-composite context, some with offensive context
		assertFalse(durabilityAttrs.isEmpty(),
				"Iron must have durability attributes");

		// The metal_upgrade_base offensive bonuses now gate themselves with an in_slot_type
		// condition on the offensive context (instead of a scope label).
		boolean hasOffensiveContextCondition = iron.properties(Attribute.KEY).stream()
				.anyMatch(attr -> attr.condition()
						.map(c -> c.staticConditions().stream()
								.anyMatch(sc -> sc instanceof InSlotTypeCondition slot
										&& slot.slotType().equals(OFFENSIVE_CONTEXT)))
						.orElse(false));

		// The rarity/weight bonuses keep the upgrade scope.
		boolean hasUpgradeScope = iron.properties(Attribute.KEY).stream()
				.anyMatch(attr -> attr.scope()
						.map(s -> s.equals(AttributeScope.UPGRADE))
						.orElse(false));

		// Iron should carry the upgrade-context bonuses from metal_upgrade_base
		assertTrue(hasOffensiveContextCondition || hasUpgradeScope,
				"Iron must carry metal_upgrade_base bonuses gated to the upgrade context " +
						"(offensive in_slot_type condition or scope/upgrade). " +
						"Check that the include directive is working correctly.");

		context.complete();
	}

	/**
	 * Tests that iron has the reinforcement tag required for reinforcement slots.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_has_reinforcement_tag(TestContext context) {
		var iron = ForgeroTestUtils.forgero(context).component("forgero:iron").orElseThrow();

		assertTrue(iron.getTags().contains(OpenIdentifier.parse("forgero:upgrades/types/reinforcement")),
				"Iron must have the reinforcement tag");

		context.complete();
	}

	// ========== Pickaxe Head Slot Tests ==========

	/**
	 * Tests that iron pickaxe head has a reinforcement slot with offensive context.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_pickaxe_head_has_offensive_context_reinforcement_slot(TestContext context) {
		var ironPickaxeHead = ForgeroTestUtils.forgero(context).component("forgero:iron-pickaxe_head").orElseThrow();

		assertTrue(ironPickaxeHead instanceof CustomizableComponent,
				"Iron pickaxe head must be customizable");

		CustomizableComponent customizable = (CustomizableComponent) ironPickaxeHead;
		Optional<ComponentUpgradeSlot> reinforcementSlot = customizable.upgrades().allUpgradeSlots().stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.findFirst();

		assertTrue(reinforcementSlot.isPresent(), "Iron pickaxe head must have a reinforcement slot");

		// The slot carries its offensive context as a scope, matched by in_slot_type conditions.
		assertTrue(reinforcementSlot.get().scope().isPresent(),
				"Reinforcement slot must have a scope");
		assertEquals(OFFENSIVE_CONTEXT.toString(), reinforcementSlot.get().scope().get().toString(),
				"Reinforcement slot must have offensive scope");

		context.complete();
	}

	// ========== Installation Tests ==========

	/**
	 * Tests that iron can be installed into a pickaxe head's reinforcement slot.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_can_be_installed_in_pickaxe_head_reinforcement_slot(TestContext context) {
		var ironPickaxeHead = ForgeroTestUtils.forgero(context).component("forgero:iron-pickaxe_head").orElseThrow();
		var diamond = ForgeroTestUtils.forgero(context).component("forgero:diamond").orElseThrow();

		SlotManager slotManager = ForgeroApi.slotManager();

		// Check if diamond can be installed
		boolean canInstall = slotManager.canInstall(ironPickaxeHead, diamond);
		assertTrue(canInstall, "Diamond must be installable in iron pickaxe head's reinforcement slot");

		// Actually install
		InstallationResult result = slotManager.install(ironPickaxeHead, diamond);
		assertTrue(result.success(), "Installation must succeed. Error: " + result.errorMessage().orElse(""));

		// Verify the result has the upgrade
		Component upgradedHead = result.component().orElseThrow();
		List<Component> installedUpgrades = slotManager.getInstalledUpgrades(upgradedHead);

		assertEquals(1, installedUpgrades.size(), "Upgraded head must have 1 installed upgrade");
		assertEquals(diamond.id(), installedUpgrades.get(0).id(),
				"Installed upgrade must be diamond");

		context.complete();
	}

	/**
	 * Tests that after installing iron reinforcement, the pickaxe head has increased stats.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_reinforcement_increases_pickaxe_head_stats(TestContext context) {
		var diamondPickaxeHead = ForgeroTestUtils.forgero(context).component("forgero:diamond-pickaxe_head").orElseThrow();
		var iron = ForgeroTestUtils.forgero(context).component("forgero:iron").orElseThrow();

		SlotManager slotManager = ForgeroApi.slotManager();

		// Get base stats using resolved attribute values (includes upgrades)
		float baseDurability = getResolvedAttributeValue(diamondPickaxeHead, DefaultAttributes.DURABILITY);
		float baseAttackDamage = getResolvedAttributeValue(diamondPickaxeHead, DefaultAttributes.ATTACK_DAMAGE);
		float baseMiningSpeed = getResolvedAttributeValue(diamondPickaxeHead, DefaultAttributes.MINING_SPEED);

		// Install iron
		InstallationResult result = slotManager.install(diamondPickaxeHead, iron);
		assertTrue(result.success(), "Installation must succeed. Error: " + result.errorMessage().orElse(""));

		Component upgradedHead = result.component().orElseThrow();

		// Get upgraded stats using resolved attribute values (includes upgrades)
		float upgradedDurability = getResolvedAttributeValue(upgradedHead, DefaultAttributes.DURABILITY);
		float upgradedAttackDamage = getResolvedAttributeValue(upgradedHead, DefaultAttributes.ATTACK_DAMAGE);
		float upgradedMiningSpeed = getResolvedAttributeValue(upgradedHead, DefaultAttributes.MINING_SPEED);

		// According to metal_upgrade_base:
		// - durability: +120 (context: forgero:contexts/offensive)
		// - attack_damage: +2 (context: forgero:contexts/offensive)
		// - mining_speed: +3 (context: forgero:contexts/offensive)

		// Verify stats increased
		assertTrue(upgradedDurability > baseDurability,
				String.format("Durability must increase after iron reinforcement. " +
						"Base: %f, Upgraded: %f", baseDurability, upgradedDurability));

		assertTrue(upgradedAttackDamage > baseAttackDamage,
				String.format("Attack damage must increase after iron reinforcement. " +
						"Base: %f, Upgraded: %f", baseAttackDamage, upgradedAttackDamage));

		assertTrue(upgradedMiningSpeed > baseMiningSpeed,
				String.format("Mining speed must increase after iron reinforcement. " +
						"Base: %f, Upgraded: %f", baseMiningSpeed, upgradedMiningSpeed));

		context.complete();
	}

	/**
	 * Tests that the upgrade persists in the component structure.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void upgrade_persists_in_component_structure(TestContext context) {
		var ironPickaxeHead = ForgeroTestUtils.forgero(context).component("forgero:iron-pickaxe_head").orElseThrow();
		var diamond = ForgeroTestUtils.forgero(context).component("forgero:diamond").orElseThrow();

		SlotManager slotManager = ForgeroApi.slotManager();

		// Install diamond
		InstallationResult result = slotManager.install(ironPickaxeHead, diamond);
		assertTrue(result.success(), "Installation must succeed");

		Component upgradedHead = result.component().orElseThrow();

		// Verify the slot is filled
		CustomizableComponent customizable = (CustomizableComponent) upgradedHead;
		Optional<ComponentUpgradeSlot> reinforcementSlot = customizable.upgrades().allUpgradeSlots().stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.findFirst();

		assertTrue(reinforcementSlot.isPresent(), "Reinforcement slot must exist");
		assertTrue(reinforcementSlot.get().isFilled(), "Reinforcement slot must be filled after installation");
		assertEquals(diamond.id(), reinforcementSlot.get().content().get().id(),
				"Slot content must be diamond");

		context.complete();
	}

	/**
	 * Tests that multiple materials can be used as reinforcements.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void multiple_materials_work_as_reinforcements(TestContext context) {
		String[] reinforcementMaterials = {"iron", "diamond", "netherite", "gold", "copper"};

		SlotManager slotManager = ForgeroApi.slotManager();

		for (String material : reinforcementMaterials) {
			var ironPickaxeHead = ForgeroTestUtils.forgero(context).component("forgero:iron-pickaxe_head").orElseThrow();
			var reinforcement = ForgeroTestUtils.forgero(context).component("forgero:" + material);

			assertTrue(reinforcement.isPresent(), material + " must exist");

			boolean canInstall = slotManager.canInstall(ironPickaxeHead, reinforcement.get());
			assertTrue(canInstall, material + " must be installable as reinforcement");

			InstallationResult result = slotManager.install(ironPickaxeHead, reinforcement.get());
			assertTrue(result.success(),
					material + " installation must succeed. Error: " + result.errorMessage().orElse(""));
		}

		context.complete();
	}
}
