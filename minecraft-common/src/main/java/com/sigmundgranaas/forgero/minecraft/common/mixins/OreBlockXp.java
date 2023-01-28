package com.sigmundgranaas.forgero.minecraft.common.mixins;

import net.minecraft.block.OreBlock;
import net.minecraft.util.math.intprovider.IntProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OreBlock.class)
public interface OreBlockXp {
    @Accessor
    IntProvider getExperienceDropped();
}
