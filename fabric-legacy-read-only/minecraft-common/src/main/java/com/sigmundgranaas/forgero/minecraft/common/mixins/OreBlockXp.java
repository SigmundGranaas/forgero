package com.sigmundgranaas.forgero.minecraft.common.mixins;

import net.minecraft.block.ExperienceDroppingBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.math.intprovider.IntProvider;

@Mixin(ExperienceDroppingBlock.class)
public interface OreBlockXp {
	@Accessor
	IntProvider getExperienceDropped();
}
