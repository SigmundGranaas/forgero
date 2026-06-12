package com.sigmundgranaas.forgero.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.api.ConditionContext;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.InstallationResult;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.effects.api.OnHitEffects;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end proof that the public condition-authoring path (ConditionContext) reflects a real
 * assembled component — i.e. a downstream {@code registerStaticCondition(...)} predicate would see
 * the truth. The adapter is the only non-trivial part of the F1 public extension API; this exercises
 * it against an actual installed upgrade rather than a hand-built tree.
 */
public class PublicApiExtensionTest implements ForgeroGameTest {

	private static final OpenIdentifier OFFENSIVE = OpenIdentifier.parse("forgero:contexts/offensive");

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void condition_context_reflects_root(TestContext context) {
		Component head = ForgeroTestUtils.forgero(context).component("forgero:diamond-pickaxe_head").orElseThrow();

		ConditionContext ctx = ConditionContext.of(new ResolutionContext(head, head));

		assertTrue(ctx.isRoot(), "The queried root component must report isRoot()");
		assertEquals(0, ctx.depth(), "Root depth must be 0");
		assertFalse(ctx.isInSlotType(OFFENSIVE), "The root is not in any slot");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void condition_context_reflects_installed_upgrade_slot(TestContext context) {
		Component head = ForgeroTestUtils.forgero(context).component("forgero:diamond-pickaxe_head").orElseThrow();
		Component iron = ForgeroTestUtils.forgero(context).component("forgero:iron").orElseThrow();

		InstallationResult result = ForgeroApi.slotManager().install(head, iron);
		assertTrue(result.success(), "Iron must install into the reinforcement slot");
		Component upgraded = result.component().orElseThrow();

		// The iron instance now sitting in the offensive reinforcement slot.
		Component installed = ((CustomizableComponent) upgraded).upgrades().allUpgradeSlots().stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.map(ComponentUpgradeSlot::getContent)
				.flatMap(java.util.Optional::stream)
				.findFirst()
				.orElseThrow();

		ConditionContext ctx = ConditionContext.of(new ResolutionContext(installed, upgraded));

		assertFalse(ctx.isRoot(), "An installed upgrade is not the root");
		assertTrue(ctx.depth() > 0, "An installed upgrade is below the root");
		assertTrue(ctx.isInSlotType(OFFENSIVE),
				"The public ConditionContext must report the slot's offensive context — this is what a "
						+ "downstream registerStaticCondition(\"...\", c -> c.isInSlotType(...)) predicate sees");

		context.complete();
	}

	/** An effect registered through the public facade (no config) parses + applies for real. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void public_effect_registration_no_config_fires(TestContext context) {
		// What a downstream mod writes (public-only):
		OnHitEffects.registerSingleTarget("forgero:example_torch", entity -> entity.setOnFireFor(5));

		// What content does: {"type":"forgero:example_torch"} dispatched through the effect codec.
		JsonObject json = new JsonObject();
		json.addProperty("type", "forgero:example_torch");
		OnHitEffect effect = OnHitEffect.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();

		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		((EntityEffectHandler) effect).apply(target);

		assertTrue(target.isOnFire(), "The registered effect must set the target on fire");
		context.complete();
	}

	/** An effect registered with a JSON config codec parses its config and applies it. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void public_effect_registration_with_config_fires(TestContext context) {
		Codec<Burn> codec = RecordCodecBuilder.create(instance ->
				instance.group(Codec.INT.fieldOf("seconds").forGetter(Burn::seconds)).apply(instance, Burn::new));
		OnHitEffects.registerSingleTarget("forgero:example_burn", codec, (cfg, entity) -> entity.setOnFireFor(cfg.seconds()));

		JsonObject json = new JsonObject();
		json.add("type", new JsonPrimitive("forgero:example_burn"));
		json.add("seconds", new JsonPrimitive(2));
		OnHitEffect effect = OnHitEffect.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();

		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		((EntityEffectHandler) effect).apply(target);

		assertEquals(40, target.getFireTicks(), "Config (2s) must drive the effect: 2 * 20 = 40 fire ticks");
		context.complete();
	}

	private record Burn(int seconds) {
	}
}
