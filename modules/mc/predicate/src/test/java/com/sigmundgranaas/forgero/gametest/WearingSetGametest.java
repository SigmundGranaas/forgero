package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;
import com.sigmundgranaas.forgero.predicate.minecraft.standalone.WearingSetPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Phase 5: wearing_set condition. Uses the vanilla {@code minecraft:trimmable_armor} item tag,
 * which contains all armor pieces, so no custom datapack tag is required.
 */
public class WearingSetGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;
	private static final String ARMOR_TAG = "minecraft:trimmable_armor";

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testWearingSet(TestContext context) {
		LivingEntity entity = context.spawnEntity(EntityType.ARMOR_STAND, new BlockPos(1, 1, 1));
		entity.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
		entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
		entity.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
		entity.equipStack(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));

		DynamicContext ctx = new DynamicContext.Builder()
				.put(MinecraftContextKeys.SOURCE_ENTITY, entity).build();

		context.assertTrue(new WearingSetPredicate(ARMOR_TAG, 4).test(ctx),
				"Full armor set should satisfy min_count 4");
		context.assertFalse(new WearingSetPredicate(ARMOR_TAG, 5).test(ctx),
				"There are only 4 armor slots, so min_count 5 cannot be met");

		entity.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
		context.assertFalse(new WearingSetPredicate(ARMOR_TAG, 4).test(ctx),
				"Removing a piece should drop below min_count 4");
		context.complete();
	}
}
