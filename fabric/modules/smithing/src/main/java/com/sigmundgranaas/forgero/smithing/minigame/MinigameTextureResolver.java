package com.sigmundgranaas.forgero.smithing.minigame;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;

public interface MinigameTextureResolver {
	Vec2f itemTextureOffset(ItemStack stack);

	Vec2f morphedTextureOffset(SmithingAnvilBlockEntity entity);

	Vec2f randomMarkerPosition(ItemStack stack, BlockState anvilState);

	Vec2f randomMarkerPositionMorphed(SmithingAnvilBlockEntity entity);
}
