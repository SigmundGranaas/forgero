package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;
import static net.minecraft.network.packet.c2s.common.SyncedClientOptions.createDefault;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ConnectedClientData;
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


	public static void createFloor(TestContext context) {
		TestPos center = TestPos.of(new BlockPos(0, 0, 0), context);
		Set<TestPos> blocks = BlockSelectionTest.createSquare(TestPos.of(BlockPos.ORIGIN, context), 1, 7, 7).stream().filter(pos -> !pos.absolute().equals(center.absolute())).collect(Collectors.toSet());
		BlockSelectionTest.insert(blocks, context);
	}

	public static void runDamageTest(TestContext context, ItemStack testItem, EntityType<?> testEntity, float expectedDamage) {
		createFloor(context);
		BlockPos rootPos = context.getAbsolutePos(new BlockPos(0, 1, 0)).add(-3, 0, -3);
		BlockPos relative = context.getRelativePos(rootPos);
		// Create and setup mock player with item
		ServerPlayerEntity entity = PlayerFactory.of(context, TestPos.of(rootPos, context));

		entity.setStackInHand(Hand.MAIN_HAND, testItem);

		for (int i = 0; i < 100; i++) {
			entity.playerTick();
		}

		Entity target = context.spawnEntity(testEntity, relative.up());

		// Backup initial health
		float initialHealth = ((LivingEntity) target).getHealth();

		// Attack the target with the mock player
		entity.attack(target);

		// Calculate expected health after attack
		float expectedHealthPostAttack = initialHealth - expectedDamage;

		// Assert the target's health matches expectation
		if (((LivingEntity) target).getHealth() != expectedHealthPostAttack) {
			throw new GameTestException("Expected target health to be " + expectedHealthPostAttack + " but was " + ((LivingEntity) target).getHealth());
		} else {
			context.complete();
		}
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "attribute_test", required = true)
	public void testDefaultDamageAppliesToEntity(TestContext context) {
		runDamageTest(context, new ItemStack(Items.DIAMOND_SWORD), EntityType.COW, EXPECTED_DIAMOND_SWORD_DAMAGE);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "attribute_test", required = true)
	public void testForgeroDamageAppliesToEntity(TestContext context) {
		runDamageTest(context, new ItemStack(Utils.itemFromString("forgero:diamond-sword")), EntityType.COW, EXPECTED_DIAMOND_SWORD_DAMAGE);
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
		sword = sword.copy();
		sword = sword.applyCondition(PropertyContainer.of(List.of(attribute)));
		Entity pig = new PigEntity(EntityType.PIG, context.getWorld());
		ServerPlayerEntity entity = PlayerFactory.of(context, TestPos.of(rootPos, context));
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
