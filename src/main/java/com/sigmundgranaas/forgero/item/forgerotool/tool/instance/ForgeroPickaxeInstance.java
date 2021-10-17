package com.sigmundgranaas.forgero.item.forgerotool.tool.instance;

import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPart;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public class ForgeroPickaxeInstance extends ForgeroToolInstance {
    public ForgeroPickaxeInstance(@NotNull ForgeroTool forgeroTool, @NotNull NbtCompound compound, @NotNull ForgeroToolPart head, @NotNull ForgeroToolPart handle, @NotNull ForgeroToolPart binding) {
        super(forgeroTool, compound, head, handle, binding);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.PICKAXE;
    }

    public float getMiningSpeedMultiplier() {
        return 2.0F;
    }

    public float getDamageMultiplier() {
        return 2.0F;
    }
}
