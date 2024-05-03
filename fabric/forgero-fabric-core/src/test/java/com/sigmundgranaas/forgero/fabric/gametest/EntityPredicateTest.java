package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.testutil.Items.NETHERITE_PATH_MINING_PICKAXE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityPredicate;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

@SuppressWarnings("unused")
public class EntityPredicateTest {
	public static BlockPos POS = new BlockPos(3, 3, 3);

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entityPredicateTest")
	public void testEntityJsonFlagFilter(TestContext context) {
		TestPos center = TestPos.of(POS, context);

		EntityPredicate predicate = entityPredicate();
		ServerPlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.CREATIVE)
				.stack(NETHERITE_PATH_MINING_PICKAXE)
				.pos(center.absolute())
				.build()
				.createPlayer();


		player.setSneaking(true);

		assertTrue(predicate.test(player));
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
}
