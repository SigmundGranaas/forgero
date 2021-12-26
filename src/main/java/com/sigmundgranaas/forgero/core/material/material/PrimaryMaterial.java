package com.sigmundgranaas.forgero.core.material.material;

import net.minecraft.item.ToolMaterial;

public interface PrimaryMaterial extends ForgeroMaterial, ToolMaterial {
    String getName();

    int getStiffNess();

    int getSharpness();

    int getFlexibility();

    int getEnchantability();

}
