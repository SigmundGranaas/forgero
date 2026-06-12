package com.sigmundgranaas.forgero.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.api.ConditionContext;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.InstallationResult;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.effects.api.BlockEffects;
import com.sigmundgranaas.forgero.effects.api.OnHitEffects;
import com.sigmundgranaas.forgero.effects.api.UseEffects;
import com.sigmundgranaas.forgero.effects.block.OnHitBlockEffect;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseHandler;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
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

	/** A block effect registered through the public facade parses + applies to the world. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void public_block_effect_fires(TestContext context) {
		BlockEffects.register("forgero:example_place",
				(world, source, pos) -> world.setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState()));

		JsonObject json = new JsonObject();
		json.addProperty("type", "forgero:example_place");
		OnHitBlockEffect effect = OnHitBlockEffect.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();

		BlockPos relative = new BlockPos(1, 2, 1);
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		effect.apply(context.getWorld(), source, context.getAbsolutePos(relative));

		assertEquals(Blocks.GOLD_BLOCK, context.getBlockState(relative).getBlock(),
				"The registered block effect must place the block");
		context.complete();
	}

	/** A use effect registered through the public facade parses + applies to the user. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void public_use_effect_fires(TestContext context) {
		UseEffects.register("forgero:example_ignite_self", (user, stack, hand) -> user.setOnFireFor(4));

		JsonObject json = new JsonObject();
		json.addProperty("type", "forgero:example_ignite_self");
		UseHandler handler = UseHandler.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();

		LivingEntity user = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		((SimpleUseHandler) handler).apply(user, ItemStack.EMPTY, Hand.MAIN_HAND);

		assertTrue(user.isOnFire(), "The registered use effect must set the user on fire");
		context.complete();
	}

	/** Registry-wide discovery returns real ItemStacks and the convenience tags actually resolve. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void public_discovery_finds_materials_and_parts(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();

		List<ItemStack> materials = query.allMaterials();
		assertFalse(materials.isEmpty(),
				"allMaterials() must discover loaded materials — empty means the forgero:materials tag did not resolve");
		assertTrue(materials.size() >= 10, "expected many materials, got " + materials.size());
		assertTrue(materials.stream().allMatch(query::isForgeroItem), "every discovered material is a Forgero item");

		// The right tag, proven by a known member: iron must be discoverable.
		Optional<ItemStack> iron = ForgeroApi.converter().toStack(OpenIdentifier.parse("forgero:iron"));
		assertTrue(iron.isPresent(), "iron must exist");
		assertTrue(materials.stream().anyMatch(stack -> stack.isOf(iron.get().getItem())),
				"allMaterials() must include iron");

		List<ItemStack> parts = query.allParts();
		assertFalse(parts.isEmpty(), "allParts() must discover loaded parts");

		// Inheritance: metals are a strict subset of all materials.
		List<ItemStack> metals = query.findByTag(OpenIdentifier.parse("forgero:materials/types/metal"));
		assertFalse(metals.isEmpty(), "findByTag(materials/types/metal) must find metals");
		assertTrue(metals.size() <= materials.size(), "metals must be a subset of materials");

		assertTrue(query.findByTag(OpenIdentifier.parse("forgero:definitely_not_a_tag")).isEmpty(),
				"an unknown tag yields an empty list");

		context.complete();
	}
}
