package com.sigmundgranaas.forgero.minecraft.common.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.OreBlock;
import net.minecraft.util.math.intprovider.UniformIntProvider;


@Mixin(OreBlock.class)
public interface OreBlockXp {
	@Accessor
	UniformIntProvider getExperienceDropped();
}
