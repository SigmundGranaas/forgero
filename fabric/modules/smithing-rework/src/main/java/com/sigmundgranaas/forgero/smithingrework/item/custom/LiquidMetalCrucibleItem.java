package com.sigmundgranaas.forgero.smithingrework.item.custom;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.smithingrework.block.custom.MoldBlock;
import com.sigmundgranaas.forgero.smithingrework.block.entity.MoldBlockEntity;
import com.sigmundgranaas.forgero.smithingrework.recipe.MetalMoldRecipe;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class LiquidMetalCrucibleItem extends Item {
	private static final int MAX_LIQUID_QUANTITY = 64;

	public LiquidMetalCrucibleItem(Settings settings) {
		super(settings);
	}

	public boolean hasLiquid(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains("liquid") && nbt.contains("quantity");
	}

	public boolean canAddLiquid(ItemStack stack, Identifier liquid, int amount) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null) {
			return amount <= MAX_LIQUID_QUANTITY;
		}
		Identifier currentLiquid = new Identifier(nbt.getString("liquid"));
		int currentQuantity = nbt.getInt("quantity");
		return (currentLiquid.equals(liquid) || currentQuantity == 0)
				&& (currentQuantity + amount <= MAX_LIQUID_QUANTITY);
	}

	public boolean addLiquid(ItemStack stack, Identifier liquid, int amount) {
		if (canAddLiquid(stack, liquid, amount)) {
			NbtCompound nbt = stack.getOrCreateNbt();
			nbt.putString("liquid", liquid.toString());
			nbt.putInt("quantity", nbt.getInt("quantity") + amount);
			return true;
		}
		return false;
	}

	public Identifier getLiquidType(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains("liquid") ? new Identifier(nbt.getString("liquid")) : null;
	}

	public int getLiquidQuantity(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null ? nbt.getInt("quantity") : 0;
	}

	public void clear(ItemStack stack) {
		stack.setNbt(null);
	}

	public boolean canUseForRecipe(ItemStack stack, Identifier requiredLiquid, int requiredAmount) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null) return false;
		Identifier currentLiquid = new Identifier(nbt.getString("liquid"));
		int currentQuantity = nbt.getInt("quantity");
		return currentLiquid.equals(requiredLiquid) && currentQuantity >= requiredAmount;
	}

	public boolean consumeLiquid(ItemStack stack, int amount) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null) return false;
		int currentQuantity = nbt.getInt("quantity");
		if (currentQuantity >= amount) {
			int newQuantity = currentQuantity - amount;
			if (newQuantity > 0) {
				nbt.putInt("quantity", newQuantity);
			} else {
				stack.setNbt(null);
			}
			return true;
		}
		return false;
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		if (hasLiquid(stack)) {
			Identifier liquidType = getLiquidType(stack);
			int quantity = getLiquidQuantity(stack);
			tooltip.add(Text.literal(String.format("%s: %d mB", liquidType, quantity)));
		} else {
			tooltip.add(Text.literal("Empty"));
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.NONE);

		if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = hitResult.getBlockPos();
			BlockState state = world.getBlockState(pos);

			if (state.getBlock() instanceof MoldBlock) {
				if (!world.isClient) {
					return handleMoldInteraction(world, player, stack, pos);
				}
				return TypedActionResult.success(stack);
			}
		}

		return super.use(world, player, hand);
	}

	private TypedActionResult<ItemStack> handleMoldInteraction(World world, PlayerEntity player, ItemStack stack, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		MoldBlockEntity entity = (MoldBlockEntity) world.getBlockEntity(pos);

		if (state.get(MoldBlock.FILLED)) {
			player.sendMessage(Text.literal("This mold is already filled."), true);
			return TypedActionResult.fail(stack);
		}

		if (!entity.isEmpty()) {
			player.sendMessage(Text.literal("This mold is already filled."), true);
			return TypedActionResult.fail(stack);
		}

		NbtCompound nbt = stack.getNbt();
		if (nbt == null || !nbt.contains("liquid")) {
			player.sendMessage(Text.literal("This crucible is empty."), true);
			return TypedActionResult.fail(stack);
		}

		Identifier liquidType = new Identifier(nbt.getString("liquid"));
		int liquidAmount = nbt.getInt("quantity");

		Optional<MetalMoldRecipe> recipeOpt = world.getRecipeManager().getFirstMatch(
				MetalMoldRecipe.TYPE,
				new SimpleInventory(world.getBlockState(pos).getBlock().asItem().getDefaultStack()),
				world
		);

		if (recipeOpt.isPresent()) {
			MetalMoldRecipe recipe = recipeOpt.get();
			if (recipe.getLiquid().equals(liquidType) && liquidAmount >= recipe.getLiquidAmount()) {
				entity.pourLiquid(liquidType, recipe.getLiquidAmount(), recipe.getCoolingTime(), recipe.getOutput(world.getRegistryManager()));

				nbt.putInt("quantity", liquidAmount - recipe.getLiquidAmount());
				if (liquidAmount - recipe.getLiquidAmount() == 0) {
					nbt.remove("liquid");
				}

				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);

				player.sendMessage(Text.literal("Poured liquid metal into the mold."), true);
				return TypedActionResult.success(stack);
			} else {
				player.sendMessage(Text.literal("Not enough liquid metal in the crucible."), true);
				return TypedActionResult.fail(stack);
			}
		} else {
			player.sendMessage(Text.literal("No valid recipe found for this mold and liquid."), true);
			return TypedActionResult.fail(stack);
		}
	}
}
