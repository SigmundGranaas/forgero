package com.sigmundgranaas.forgero.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.effects.entity.FireHandler;
import com.sigmundgranaas.forgero.effects.entity.StatusEffectHandler;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.properties.minecraft.onblock.OnBlockManager;
import com.sigmundgranaas.forgero.properties.minecraft.onblock.OnBlockProperty;
import com.sigmundgranaas.forgero.properties.minecraft.oncrit.OnCritManager;
import com.sigmundgranaas.forgero.properties.minecraft.oncrit.OnCritProperty;
import com.sigmundgranaas.forgero.properties.minecraft.onequip.EquipmentChangeManager;
import com.sigmundgranaas.forgero.properties.minecraft.onequip.OnEquipProperty;
import com.sigmundgranaas.forgero.properties.minecraft.onequip.OnUnequipProperty;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * End-to-end firing tests for the new event properties (on_crit / on_block / on_equip /
 * on_unequip), using real content: the {@code forgero:example_event_aspect} upgrade installed on an
 * iron sword. Verifies both that the properties resolve onto a real item and that triggering the
 * managers applies the configured effects.
 */
public class EventPropertyFiringTest implements ForgeroGameTest {

	private static ItemStack upgradedSword(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var mutate = ctx.api().itemMutation();

		ItemStack sword = ctx.toStack(ctx.component("forgero:iron_sword").orElseThrow()).orElseThrow();
		ItemStack aspect = ctx.toStack(ctx.component("forgero:example_event_aspect").orElseThrow()).orElseThrow();
		assertTrue(mutate.canInstallUpgrade(sword, aspect), "example_event_aspect must install on iron_sword");
		return mutate.installUpgrade(sword, aspect);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void event_properties_resolve_onto_real_item(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		Component component = ctx.toComponent(upgradedSword(context)).orElseThrow();

		assertTrue(new OnCritProperty.Engine().resolve(component).stream()
				.flatMap(p -> p.effects().stream()).anyMatch(e -> e instanceof FireHandler),
				"on_crit fire effect should resolve onto the upgraded sword");
		assertTrue(new OnBlockProperty.Engine().resolve(component).stream()
				.flatMap(p -> p.effects().stream()).anyMatch(e -> e instanceof FireHandler),
				"on_block fire effect should resolve");
		assertTrue(new OnEquipProperty.Engine().resolve(component).stream()
				.flatMap(p -> p.effects().stream()).anyMatch(e -> e instanceof StatusEffectHandler),
				"on_equip status effect should resolve");
		assertTrue(new OnUnequipProperty.Engine().resolve(component).stream()
				.flatMap(p -> p.effects().stream()).anyMatch(e -> e instanceof StatusEffectHandler),
				"on_unequip status effect should resolve");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void on_crit_applies_effect_to_victim(TestContext context) {
		PlayerEntity attacker = context.createMockCreativeServerPlayerInWorld();
		attacker.setStackInHand(Hand.MAIN_HAND, upgradedSword(context));
		LivingEntity victim = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		OnCritManager.handleCrit(attacker, victim);

		assertTrue(victim.isOnFire(), "on_crit should ignite the victim");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void on_block_applies_effect_to_attacker(TestContext context) {
		PlayerEntity defender = context.createMockCreativeServerPlayerInWorld();
		defender.setStackInHand(Hand.MAIN_HAND, upgradedSword(context));
		LivingEntity attacker = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		OnBlockManager.handleBlock(defender, attacker);

		assertTrue(attacker.isOnFire(), "on_block (parry) should ignite the attacker");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void on_equip_and_unequip_apply_effects_to_wearer(TestContext context) {
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		EquipmentChangeManager.clearAll();

		// Snapshot with an empty main hand so the equip is detected as a change.
		player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
		EquipmentChangeManager.handleTick(player);

		player.setStackInHand(Hand.MAIN_HAND, upgradedSword(context));
		EquipmentChangeManager.handleTick(player);
		assertTrue(player.hasStatusEffect(StatusEffects.SPEED), "on_equip should grant speed when wielded");

		player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
		EquipmentChangeManager.handleTick(player);
		assertTrue(player.hasStatusEffect(StatusEffects.GLOWING), "on_unequip should apply glowing when removed");

		EquipmentChangeManager.clearAll();
		context.complete();
	}
}
