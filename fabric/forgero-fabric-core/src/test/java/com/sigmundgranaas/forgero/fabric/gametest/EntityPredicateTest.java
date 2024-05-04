package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityFlagPredicates.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityPredicate;
import com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityTypePredicate;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;
import org.junit.jupiter.api.Assertions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

@SuppressWarnings("unused")
public class EntityPredicateTest {
	public static BlockPos POS = new BlockPos(3, 3, 3);

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entityPredicateTest")
	public void testEntityJsonFlagFilter(TestContext context) {
		TestPos center = TestPos.of(POS, context);

		EntityPredicate predicate = entityPredicate();
		ServerPlayerEntity player = PlayerFactory.builder(context)
				.pos(center.absolute())
				.build()
				.createPlayer();

		player.setSneaking(true);

		assertTrue(predicate.test(player));
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entityPredicateTest")
	public void testEntityTypeJson(TestContext context) {
		TestPos center = TestPos.of(POS, context);

		EntityPredicate predicate = entityTypePredicate();
		ServerPlayerEntity player = PlayerFactory.builder(context)
				.pos(center.absolute())
				.build()
				.createPlayer();

		Entity pig = new PigEntity(EntityType.PIG, context.getWorld());

		assertTrue(predicate.test(pig));
		assertFalse(predicate.test(player));

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entityPredicateTest")
	public void testEntityType(TestContext context) {
		TestPos center = TestPos.of(POS, context);

		EntityTypePredicate pigPredicate = new EntityTypePredicate(new Identifier("minecraft:pig"));
		EntityTypePredicate itemPredicate = new EntityTypePredicate(new Identifier("minecraft:item"));

		Entity pig = new PigEntity(EntityType.PIG, context.getWorld());
		Entity item = new ItemEntity(context.getWorld(), center.absolute().getX(), center.absolute().getY(), center.absolute().getZ(), new ItemStack(Items.OAK_PLANKS));

		assertTrue(pigPredicate.test(pig.getType()));
		assertFalse(pigPredicate.test(item.getType()));

		assertTrue(itemPredicate.test(item.getType()));
		assertFalse(itemPredicate.test(pig.getType()));

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entityPredicateTest")
	public void testEncodeDecode(TestContext context) {
		EntityPredicate predicate = entityTypePredicate();
		JsonElement json = EntityPredicate.CODEC.encode(predicate, JsonOps.INSTANCE, new JsonObject()).result().get();

		Assertions.assertEquals(JsonParser.parseString(entityPredicate), json);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entityPredicateTest")
	public void testFlagPredicates(TestContext context) {
		TestPos center = TestPos.of(POS, context);
		ServerPlayerEntity player = PlayerFactory.builder(context)
				.pos(center.absolute())
				.build()
				.createPlayer();

		player.setSneaking(true);
		assertTrue(IS_SNEAKING.value().test(player));
		player.setSneaking(false);
		assertFalse(IS_SNEAKING.value().test(player));

		player.setSwimming(true);
		assertTrue(IS_SWIMMING.value().test(player));
		player.setSwimming(false);
		assertFalse(IS_SWIMMING.value().test(player));

		player.setSprinting(true);
		assertTrue(IS_SPRINTING.value().test(player));
		player.setSprinting(false);
		assertFalse(IS_SPRINTING.value().test(player));


		player.setOnGround(true);
		assertTrue(IS_ON_GROUND.value().test(player));
		player.setOnGround(false);
		assertFalse(IS_ON_GROUND.value().test(player));

		context.complete();
	}


	public static EntityPredicate entityPredicate() {
		String filter = """
				{
					"type": "minecraft:entity",
				    "flags": {
				      "is_sneaking": true
				    }
				}
				""";
		return EntityPredicate.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(filter)).result().get().getFirst();
	}

	public static String entityPredicate = """
			{
				"type": "minecraft:entity",
			    "entity_type": {
			      "id": "minecraft:pig"
			    }
			}
			""";

	public static EntityPredicate entityTypePredicate() {
		return EntityPredicate.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(entityPredicate)).result().get().getFirst();
	}
}
