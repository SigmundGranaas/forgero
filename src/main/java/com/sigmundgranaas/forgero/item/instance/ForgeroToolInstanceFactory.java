package com.sigmundgranaas.forgero.item.instance;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.item.instance.tool.ForgeroToolInstance;
import com.sigmundgranaas.forgero.item.tool.ForgeroToolItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ForgeroToolInstanceFactory {
    ForgeroToolInstanceFactory INSTANCE = ForgeroToolInstanceFactoryImpl.getInstance();

    @NotNull
    ForgeroToolInstance createForgeroToolInstance(ForgeroToolItem tool, NbtCompound compound);

    @NotNull
    ForgeroToolInstance createForgeroToolInstance(ForgeroTool tool);


    @NotNull
    ForgeroToolInstance CreateForgeroBaseToolInstance(ForgeroToolItem tool);

    @NotNull
    Optional<ForgeroToolInstance> CreateForgeroBaseToolInstance(ItemStack stack);
}
