package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.DefaultWriter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(Item.class)
public class ItemDescriptionInjector {

    @SuppressWarnings("DataFlowIssue")
    @Inject(at = @At("HEAD"), method = "appendTooltip")
    public void forgero$InjectForgeroAttributes(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        Item item = (Item) (Object) this;
        String id = Registry.ITEM.getId(item).toString();
        if (!(item instanceof StateItem) && ForgeroStateRegistry.CONTAINER_TO_STATE.containsKey(id)) {
            if (Screen.hasShiftDown()) {
                ForgeroStateRegistry.STATES
                        .find(ForgeroStateRegistry.CONTAINER_TO_STATE.get(id))
                        .map(Supplier::get)
                        .ifPresent(state -> new DefaultWriter(state).write(tooltip, context));
            } else {
                tooltip.add(Text.translatable("tooltip.forgero.hold").formatted(Formatting.DARK_GRAY)
                        .append(Text.translatable("tooltip.forgero.shift").formatted(Formatting.WHITE))
                        .append(Text.translatable("tooltip.forgero.to_show_attributes").formatted(Formatting.DARK_GRAY))
                );
            }
        }
    }
}
