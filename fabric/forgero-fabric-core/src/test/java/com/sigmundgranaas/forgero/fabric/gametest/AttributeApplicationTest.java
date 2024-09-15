package com.sigmundgranaas.forgero.fabric.gametest;

import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.service.StateService;
import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class AttributeApplicationTest {

	public static float EXPECTED_DIAMOND_SWORD_DAMAGE = 7f;

	public static ServerPlayerEntity createMockPlayer(BlockPos pos, TestContext context) {
		context.getWorld().getServer().setDemo(false);
		ServerPlayerEntity entity = new ServerPlayerEntity(context.getWorld().getServer(), context.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-player"));
		entity.networkHandler = new ServerPlayNetworkHandler(context.getWorld().getServer(), new ClientConnection(NetworkSide.CLIENTBOUND), entity);
		entity.setPos(pos.getX(), pos.getY(), pos.getZ());
		return entity;
	}

	public static void createFloor(TestContext context) {
		BlockSelectionTest.insert(BlockSelectionTest.createSquare(TestPos.of(BlockPos.ORIGIN, context), 1, 7, 7), context);
	}

	public static void runDamageTest(TestContext context, ItemStack testItem, EntityType<?> testEntity, float expectedDamage) {
		createFloor(context);
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 0, -3);
		BlockPos relative = context.getRelativePos(rootPos);
		// Create and setup mock player with item
		ServerPlayerEntity entity = createMockPlayer(relative, context);

		entity.setStackInHand(Hand.MAIN_HAND, testItem);

		for (int i = 0; i < 100; i++) {
			entity.playerTick();
		}

		Entity target = context.spawnEntity(testEntity, relative);

		// Backup initial health
		float initialHealth = ((LivingEntity) target).getHealth();

		// Attack the target with the mock player
		entity.attack(target);

		// Calculate expected health after attack
		float expectedHealthPostAttack = initialHealth - expectedDamage;

		// Assert the target's health matches expectation
		if (((LivingEntity) target).getHealth() != expectedHealthPostAttack) {

			// If this is a real test framework, you might have an assert or fail method.
			// Here's a hypothetical way to report a failure in the test:
			throw new GameTestException("Expected target health to be " + expectedHealthPostAttack + " but was " + (initialHealth - ((LivingEntity) target).getHealth()));
		} else {
			context.complete();
		}
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "attribute_test", required = true)
	public void testDefaultDamageAppliesToEntity(TestContext context) {
		runDamageTest(context, new ItemStack(Items.DIAMOND_SWORD), EntityType.PIG, EXPECTED_DIAMOND_SWORD_DAMAGE);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "attribute_test", required = true)
	public void testForgeroDamageAppliesToEntity(TestContext context) {
		runDamageTest(context, new ItemStack(Utils.itemFromString("forgero:diamond-sword")), EntityType.PIG, EXPECTED_DIAMOND_SWORD_DAMAGE);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "attribute_test", required = true)
	public void testEntityPredicateBonus(TestContext context) {
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 0, 0)).add(-3, 0, -3);
		BlockPos relative = context.getRelativePos(rootPos);

		var object = new JsonObject();
		var pigObject = new JsonObject();
		pigObject.addProperty("id", "minecraft:pig");

		object.addProperty("type", "minecraft:entity");
		object.add("entity_type", pigObject);

		Matchable pigPredicate = new PredicateFactory().create(object);
		Attribute attribute = AttributeBuilder.builder(AttackDamage.KEY)
				.applyPredicate(pigPredicate)
				.build();

		ConstructedTool sword = (ConstructedTool) StateService.INSTANCE.find(new Identifier("forgero:diamond-sword")).get();
		sword = sword.applyCondition(PropertyContainer.of(List.of(attribute)));
		Entity pig = new PigEntity(EntityType.PIG, context.getWorld());
		ServerPlayerEntity entity = createMockPlayer(relative, context);
		MatchContext matchContext = MatchContext.of()
				.put(WORLD, context.getWorld())
				.put(ENTITY_TARGET, pig)
				.put(ENTITY, entity);

		float damage = sword.stream(Matchable.DEFAULT_TRUE, matchContext).applyAttribute(AttackDamage.KEY);
		float damageNotPig = sword.stream(Matchable.DEFAULT_TRUE, MatchContext.of()).applyAttribute(AttackDamage.KEY);

		if (damage == EXPECTED_DIAMOND_SWORD_DAMAGE + 1f && damageNotPig == EXPECTED_DIAMOND_SWORD_DAMAGE) {
			context.complete();
		} else {
			throw new GameTestException("Expected damage to be " + EXPECTED_DIAMOND_SWORD_DAMAGE + 1f + " but was " + damage);
		}
	}
}
