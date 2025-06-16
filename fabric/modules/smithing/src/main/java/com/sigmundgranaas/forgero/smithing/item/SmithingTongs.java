package com.sigmundgranaas.forgero.smithing.item;

import com.sigmundgranaas.forgero.smithing.component.HeatedItemComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SmithingTongs extends Item {
	private static final String HELD_ITEM_NBT_KEY = "held_item";

	public SmithingTongs(Settings settings) {
		super(settings.maxCount(1));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack tongs = user.getStackInHand(hand);
		ItemStack heldItem = getHeldItem(tongs);

		if (!heldItem.isEmpty()) {
			// Drop the held item
			if (!user.getInventory().insertStack(heldItem)) {
				user.dropItem(heldItem, false);
			}
			setHeldItem(tongs, ItemStack.EMPTY);

			if (!world.isClient) {
				world.playSound(null, user.getX(), user.getY(), user.getZ(),
						SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundCategory.PLAYERS, 0.5f, 1.2f);
				user.sendMessage(Text.literal("Released item from tongs").formatted(Formatting.GREEN), true);
			}

			return TypedActionResult.success(tongs);
		}

		return TypedActionResult.pass(tongs);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		ItemStack tongs = context.getStack();

		if (player == null) return ActionResult.PASS;

		// Check if we're trying to pick up an item from an anvil or other block entity
		// This would need to be implemented in your block entities

		return ActionResult.PASS;
	}

	public static ItemStack getHeldItem(ItemStack tongs) {
		if (tongs.hasNbt() && tongs.getNbt().contains(HELD_ITEM_NBT_KEY)) {
			return ItemStack.fromNbt(tongs.getNbt().getCompound(HELD_ITEM_NBT_KEY));
		}
		return ItemStack.EMPTY;
	}

	public static void setHeldItem(ItemStack tongs, ItemStack heldItem) {
		if (heldItem.isEmpty()) {
			if (tongs.hasNbt()) {
				tongs.getNbt().remove(HELD_ITEM_NBT_KEY);
			}
		} else {
			tongs.getOrCreateNbt().put(HELD_ITEM_NBT_KEY, heldItem.writeNbt(new net.minecraft.nbt.NbtCompound()));
		}
	}

	public static boolean canPickupItem(ItemStack item) {
		// Can pickup any item, but hot items require tongs
		return true;
	}

	public static boolean isEmpty(ItemStack tongs) {
		return getHeldItem(tongs).isEmpty();
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, java.util.List<net.minecraft.text.Text> tooltip, net.minecraft.client.item.TooltipContext context) {
		ItemStack heldItem = getHeldItem(stack);
		if (!heldItem.isEmpty()) {
			tooltip.add(Text.literal("Holding: ").formatted(Formatting.GRAY)
					.append(heldItem.getName().copy().formatted(Formatting.WHITE)));

			if (HeatedItemComponent.isHot(heldItem)) {
				tooltip.add(HeatedItemComponent.getHeatText(heldItem));
			}
		} else {
			tooltip.add(Text.literal("Right-click to release held items").formatted(Formatting.GRAY));
			tooltip.add(Text.literal("Use on hot items to pick them up safely").formatted(Formatting.GRAY));
		}
	}
}
