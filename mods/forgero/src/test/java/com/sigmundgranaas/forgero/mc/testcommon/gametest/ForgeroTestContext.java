package com.sigmundgranaas.forgero.mc.testcommon.gametest;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.ForgeroServices;
import net.minecraft.util.math.BlockPos;
import net.minecraft.test.TestContext;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.BlockEntity;

import java.util.Optional;

public class ForgeroTestContext {

    private final TestContext context;
    private final ForgeroServices api;

    public ForgeroTestContext(TestContext context) {
        this.context = context;
        this.api = ForgeroApi.services();
    }

    public Optional<Component> component(String identifier) {
        return api.componentRegistry().get(OpenIdentifier.parse(identifier));
    }

    public Optional<Component> component(OpenIdentifier identifier) {
        return api.componentRegistry().get(identifier);
    }

    public Optional<ItemStack> toStack(Component component) {
        return api.converter().toStack(component);
    }

    public Optional<Component> toComponent(ItemStack stack) {
        return api.converter().toComponent(stack);
    }

    public ForgeroServices api() {
        return api;
    }

    public TestContext context() {
        return context;
    }

    public void assertTrue(boolean condition, String message) {
        context.assertTrue(condition, message);
    }

    public void assertFalse(boolean condition, String message) {
        context.assertFalse(condition, message);
    }

    public void complete() {
        context.complete();
    }

    public BlockEntity getBlockEntity(BlockPos pos) {
        return context.getWorld().getBlockEntity(context.getAbsolutePos(pos));
    }

    public BlockPos getAbsolutePos(BlockPos pos) {
        return context.getAbsolutePos(pos);
    }

    public BlockPos getRelativePos(BlockPos pos) {
        return context.getRelativePos(pos);
    }
}
