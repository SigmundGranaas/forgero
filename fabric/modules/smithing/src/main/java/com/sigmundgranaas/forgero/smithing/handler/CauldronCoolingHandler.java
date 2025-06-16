package com.sigmundgranaas.forgero.smithing.handler;

import com.sigmundgranaas.forgero.smithing.component.HeatedItemComponent;
import com.sigmundgranaas.forgero.smithing.item.SmithingTongs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CauldronCoolingHandler {

	public static ActionResult handleCauldronInteraction(World world, BlockPos pos, BlockState state,
														 PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		ItemStack stackInHand = player.getStackInHand(hand);

		// Check if it's a water cauldron
		if (!state.isOf(Blocks.WATER_CAULDRON)) {
			return ActionResult.PASS;
		}

		int waterLevel = state.get(LeveledCauldronBlock.LEVEL);
		if (waterLevel <= 0) {
			return ActionResult.PASS;
		}

		ItemStack itemToCool = null;
		boolean usingTongs = false;

		if (stackInHand.getItem() instanceof SmithingTongs) {
			itemToCool = SmithingTongs.getHeldItem(stackInHand);
			usingTongs = true;
		} else if (HeatedItemComponent.isHot(stackInHand)) {
			if (HeatedItemComponent.canPickupWithHands(stackInHand)) {
				itemToCool = stackInHand;
			} else {
				player.sendMessage(Text.literal("The item is too hot! Use tongs to safely cool it in water.")
						.formatted(Formatting.RED), true);
				return ActionResult.FAIL;
			}
		}

		if (itemToCool != null && HeatedItemComponent.isHot(itemToCool)) {
			// Cool the item
			int currentHeat = HeatedItemComponent.getHeat(itemToCool);
			int coolingAmount = Math.min(currentHeat, 400); // Cool by up to 400 heat points

			HeatedItemComponent.setHeat(itemToCool, currentHeat - coolingAmount);

			// Reduce water level
			world.setBlockState(pos, state.with(LeveledCauldronBlock.LEVEL, waterLevel - 1));

			// Play cooling sound effects
			world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.8f, 1.0f);
			world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.5f, 1.5f);

			// Create steam particles (you'd need to implement this)
			// spawnSteamParticles(world, pos);

			String coolingMessage = usingTongs ? "Cooled item with tongs" : "Cooled item in water";
			player.sendMessage(Text.literal(coolingMessage).formatted(Formatting.AQUA), true);
			player.sendMessage(HeatedItemComponent.getHeatText(itemToCool), true);

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	// You can add this method to spawn steam particles
    /*
    private static void spawnSteamParticles(World world, BlockPos pos) {
        if (world.isClient) {
            Random random = world.getRandom();
            for (int i = 0; i < 10; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                double y = pos.getY() + 1.0;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;

                world.addParticle(ParticleTypes.CLOUD, x, y, z, 0, 0.1, 0);
            }
        }
    }
    */
}
