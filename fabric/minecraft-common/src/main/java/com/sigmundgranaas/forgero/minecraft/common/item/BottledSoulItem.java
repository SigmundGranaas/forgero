package com.sigmundgranaas.forgero.minecraft.common.item;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulParser;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.SoulWriter;
import com.sigmundgranaas.forgero.minecraft.common.utils.Text;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BottledSoulItem extends Item {
	public BottledSoulItem(Settings settings) {
		super(settings);
	}


	@Override
	public net.minecraft.text.Text getName(ItemStack stack) {
		Optional<Soul> soul = SoulParser.of(stack);
		if (soul.isPresent()) {
			return Text.literal("Bottled ").append(Text.translatable(soul.get().name()).append(" Soul"));
		}
		return super.getName(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<net.minecraft.text.Text> tooltip, TooltipContext context) {
		SoulParser.of(stack).ifPresent(soul -> new SoulWriter(soul).write(tooltip, context));
		super.appendTooltip(stack, world, tooltip, context);
	}
}
