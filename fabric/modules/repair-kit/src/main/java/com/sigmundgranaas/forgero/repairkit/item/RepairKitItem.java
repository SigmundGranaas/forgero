package com.sigmundgranaas.forgero.repairkit.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class RepairKitItem extends Item {
	public static final int MAX_MATERIAL = 64;

	public RepairKitItem(Settings settings) {
		super(settings.maxCount(1));
	}

	private void addMaterial(ItemStack repairKit, Item material, int quantityToAdd) {
		NbtCompound tag = repairKit.getOrCreateNbt();

		String currentMaterial = tag.getString("Material");
		String addingMaterial = fromItem(material).toString();
		int currentQuantity = tag.getInt("Quantity");

		if (currentMaterial.isEmpty() || currentMaterial.equals(addingMaterial)) {
			int newQuantity = Math.min(currentQuantity + quantityToAdd, MAX_MATERIAL);

			tag.putString("Material", addingMaterial);
			tag.putInt("Quantity", newQuantity);
		}
	}

	private Identifier fromItem(Item item) {
		return Registry.ITEM.getId(item);
	}

	private Item fromString(String item) {
		return Registry.ITEM.get(new Identifier(item));
	}

	private boolean canAddMaterial(ItemStack repairKit, Item material) {
		TagKey<Item> key = TagKey.of(Registry.ITEM_KEY, new Identifier("c:repair_material"));
		if (!new ItemStack(material).isIn(key)) {
			return false;
		}
		if (repairKit.getOrCreateNbt().getInt("Quantity") >= MAX_MATERIAL) {
			return false;
		}
		NbtCompound tag = repairKit.getOrCreateNbt();

		String currentMaterial = tag.getString("Material");
		return currentMaterial.isEmpty() || currentMaterial.equals(fromItem(material).toString());
	}

	ItemStack getRepairMaterial(ItemStack repairKit) {
		NbtCompound tag = repairKit.getOrCreateNbt();
		return new ItemStack(fromString(tag.getString("Material")));
	}

	int getMaterialAmount(ItemStack repairKit) {
		return repairKit.getOrCreateNbt().getInt("Quantity");
	}

	void consumeMaterial(ItemStack repairKit) {
		repairKit.getOrCreateNbt().putInt("Quantity", getMaterialAmount(repairKit) - 1);

		if (getMaterialAmount(repairKit) == 0) {
			repairKit.getOrCreateNbt().remove("Material");
			repairKit.getOrCreateNbt().remove("Quantity");

		}
	}

	boolean hasEnoughMaterials(ItemStack repairKit) {
		return getMaterialAmount(repairKit) > 0;
	}

	boolean canRepairTool(ItemStack stack, ItemStack repairKit) {
		if (stack.getItem().isDamageable() && stack.getDamage() > 0) {
			if (stack.getItem() instanceof ToolItem tool) {
				return tool.getMaterial().getRepairIngredient().test(getRepairMaterial(repairKit)) && hasEnoughMaterials(repairKit);
			}
		}
		return false;
	}


	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack repairKit = user.getStackInHand(hand);

		if (!world.isClient()) {
			ItemStack otherHandItem = (hand == Hand.MAIN_HAND) ? user.getOffHandStack() : user.getMainHandStack();
			Item otherItem = otherHandItem.getItem();

			// Attempt to repair tool
			if (otherItem.isDamageable() && otherHandItem.getDamage() > 0) {
				if (canRepairTool(otherHandItem, repairKit)) {
					int repairAmount = otherHandItem.getMaxDamage() / 3;
					otherHandItem.setDamage(otherHandItem.getDamage() - repairAmount);
					consumeMaterial(repairKit);

					world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
					for (int i = 0; i < 5; i++) {
						double d0 = user.getRandom().nextGaussian() * 0.02D;
						double d1 = user.getRandom().nextGaussian() * 0.02D;
						double d2 = user.getRandom().nextGaussian() * 0.02D;
						world.addParticle(ParticleTypes.HAPPY_VILLAGER, user.getX() + user.getRandom().nextDouble() * user.getWidth() * 2.0F - user.getWidth(), user.getY() + 0.5D + user.getRandom().nextDouble() * user.getHeight(), user.getZ() + user.getRandom().nextDouble() * user.getWidth() * 2.0F - user.getWidth(), d0, d1, d2);
					}

					return new TypedActionResult<>(ActionResult.SUCCESS, repairKit);
				} else {
					world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1.0F, 0.5F);
					return new TypedActionResult<>(ActionResult.FAIL, repairKit);
				}
			}

			// Attempt to add material
			else if (canAddMaterial(repairKit, otherItem)) {
				addMaterial(repairKit, otherItem, 1);
				otherHandItem.decrement(1);

				world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
				for (int i = 0; i < 5; i++) {
					double d0 = user.getRandom().nextGaussian() * 0.02D;
					double d1 = user.getRandom().nextGaussian() * 0.02D;
					double d2 = user.getRandom().nextGaussian() * 0.02D;
					world.addParticle(ParticleTypes.COMPOSTER, user.getX() + user.getRandom().nextDouble() * user.getWidth() * 2.0F - user.getWidth(), user.getY() + 0.5D + user.getRandom().nextDouble() * user.getHeight(), user.getZ() + user.getRandom().nextDouble() * user.getWidth() * 2.0F - user.getWidth(), d0, d1, d2);
				}

				return new TypedActionResult<>(ActionResult.SUCCESS, repairKit);
			} else {
				world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.PLAYERS, 1.0F, 0.5F);
				return new TypedActionResult<>(ActionResult.FAIL, repairKit);
			}
		}

		return new TypedActionResult<>(ActionResult.PASS, repairKit);
	}

	@Override
	public Text getName(ItemStack stack) {
		NbtCompound tag = stack.getOrCreateNbt();
		String material = tag.getString("Material");
		if (!material.isEmpty()) {
			String translateMaterial = Registry.ITEM.get(new Identifier(material)).getTranslationKey();

			return Text.translatable(translateMaterial).append(Text.literal("  ")).append(Text.translatable("item.forgero.repair_kit"));
		}
		return Text.translatable("item.forgero.empty_repair_kit");
	}

	@Override
	public void appendTooltip(ItemStack itemStack, @Nullable World world, List<Text> tooltip, TooltipContext tooltipContext) {
		NbtCompound tag = itemStack.getOrCreateNbt();
		String material = tag.getString("Material");
		int quantity = tag.getInt("Quantity");

		if (!material.isEmpty()) {
			String translatedMaterial = Registry.ITEM.get(new Identifier(material)).getTranslationKey();
			tooltip.add(Text.literal("Material: ").append(Text.translatable(translatedMaterial)));
			tooltip.add(Text.literal("Quantity: " + quantity));
		}
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 32;
	}
}
