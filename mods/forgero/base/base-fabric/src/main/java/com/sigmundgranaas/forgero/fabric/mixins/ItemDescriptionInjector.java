package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.tooltip.Writer.writeModifierSection;

import java.util.List;

import com.sigmundgranaas.forgero.item.StateItem;
import com.sigmundgranaas.forgero.service.StateService;
import com.sigmundgranaas.forgero.tooltip.v2.DefaultWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@Mixin(Item.class)
public class ItemDescriptionInjector {

	@SuppressWarnings("DataFlowIssue")
	@Inject(at = @At("HEAD"), method = "appendTooltip")
	public void forgero$InjectForgeroAttributes(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
		Item item = (Item) (Object) this;
		String id = Registries.ITEM.getId(item).toString();
		if (!(item instanceof StateItem) && StateService.INSTANCE.find(id).isPresent()) {
			if (Screen.hasShiftDown()) {
				StateService.INSTANCE.find(id)
						.ifPresent(state -> new DefaultWriter(state).write(tooltip, context));
			} else {
				tooltip.add(writeModifierSection("shift", "to_show_attributes")
				);
			}
		}
	}
}
