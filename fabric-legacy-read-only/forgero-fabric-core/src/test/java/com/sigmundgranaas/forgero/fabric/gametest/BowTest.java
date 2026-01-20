package com.sigmundgranaas.forgero.fabric.gametest;

import com.google.gson.JsonObject;
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
import com.sigmundgranaas.forgero.testutil.TestPosCollection;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

import java.util.List;
import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.fabric.gametest.AttributeApplicationTest.createFloor;
import static com.sigmundgranaas.forgero.fabric.gametest.BlockSelectionTest.createSquare;
import static com.sigmundgranaas.forgero.fabric.gametest.BlockSelectionTest.insert;
import static com.sigmundgranaas.forgero.testutil.Utils.fromId;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BowTest {
	public static BlockPos RELATIVE_CORNER_PLAYER = new BlockPos(3, 1, 0);
	public static BlockPos OPPOSITE = new BlockPos(1, 1, 7);
	public static BlockPos MIDDLE = new BlockPos(3, 1, 4);

	public static Supplier<ItemStack> BOW = () -> fromId("forgero:oak-bow");


	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "attribute_test", required = true)
	public void testBowShooting(TestContext context) {
		TestPos center = TestPos.of(RELATIVE_CORNER_PLAYER, context);
		TestPos opposite = TestPos.of(OPPOSITE, context);
		TestPosCollection square = TestPosCollection.of(insert(createSquare(opposite, 3, 3, 1), context));


		ServerPlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.SURVIVAL)
				.direction(Direction.SOUTH)
				.stack(BOW)
				.pos(center.absolute())
				.build()
				.createPlayer();

		player.getInventory().setStack(12, fromId("forgero:stone-arrow"));
		player.getMainHandStack().use(context.getWorld(), player, Hand.MAIN_HAND);
		context.runAtEveryTick(player::tick);
		context.runAtTick(70, () -> {
					player.getMainHandStack().onStoppedUsing(context.getWorld(), player, 100);
				});



		// Verify after a couple of ticks to give the arrow time to fly
		context.runAtTick(100, () -> {
			// After shooting a single arrow, the arrow should be consumed
			assertTrue(player.getInventory().getStack(12).isEmpty());

			Box boundingBox = new Box(
					opposite.absolute().getX() - 4, opposite.absolute().getY() - 2, opposite.absolute().getZ() - 2,
					opposite.absolute().getX() + 4, opposite.absolute().getY() + 2, opposite.absolute().getZ() + 2);

			// Get entities within the bounding box
			List<Entity> entitiesWithinBox = context.getWorld().getOtherEntities(player, boundingBox);

			if(entitiesWithinBox.isEmpty()){
				context.throwGameTestException("No arrows found in the expected location!");
			}else{
				context.complete();
			}
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "attribute_test", required = true)
	public void testBowShootingLowDraw(TestContext context) {
		createFloor(context);
		TestPos center = TestPos.of(RELATIVE_CORNER_PLAYER, context);
		TestPos middle = TestPos.of(MIDDLE, context);


		ServerPlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.SURVIVAL)
				.direction(Direction.SOUTH)
				.stack(BOW)
				.pos(center.absolute())
				.build()
				.createPlayer();

		player.getInventory().setStack(12, fromId("forgero:stone-arrow"));
		player.getMainHandStack().use(context.getWorld(), player, Hand.MAIN_HAND);

		player.getMainHandStack().onStoppedUsing(context.getWorld(), player, player.getMainHandStack().getMaxUseTime() - 2);

		// After shooting a single arrow, the arrow should be consumed
		assertTrue(player.getInventory().getStack(12).isEmpty());


		// Verify after a couple of ticks to give the arrow time to fly
		context.runAtTick(20, () -> {
			Box boundingBox = new Box(
					middle.absolute().getX() - 4, middle.absolute().getY() - 2, middle.absolute().getZ() - 2,
					middle.absolute().getX() + 4, middle.absolute().getY() + 2, middle.absolute().getZ() + 2);

			// Get entities within the bounding box
			List<Entity> entitiesWithinBox = context.getWorld().getOtherEntities(player, boundingBox);

			if(entitiesWithinBox.isEmpty()){
				context.throwGameTestException("No arrows found in the expected location!");
			}else{
				context.complete();
			}
		});
	}
}
