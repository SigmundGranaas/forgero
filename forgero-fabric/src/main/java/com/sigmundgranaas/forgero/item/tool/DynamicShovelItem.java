package com.sigmundgranaas.forgero.item.tool;

import com.sigmundgranaas.forgero.item.StateItem;
import com.sigmundgranaas.forgero.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.item.tooltip.Writer;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.state.StateProvider;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DynamicShovelItem extends ShovelItem implements StateItem {
    private final StateProvider DEFAULT;

    public DynamicShovelItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, StateProvider defaultState) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public boolean isEffectiveOn(BlockState state) {
        return state.isIn(BlockTags.SHOVEL_MINEABLE);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return StateItem.super.getItemBarStep(stack);
    }

    public int getItemBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) this.getDurability(stack) - (float) stack.getDamage()) / (float) this.getDurability(stack));
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        StateWriter.of(state(stack)).write(tooltip, context);

        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public Text getName() {
        return Writer.nameToTranslatableText(this);
    }

    @Override
    public Text getName(ItemStack stack) {
        return getName();
    }

    @Override
    public State defaultState() {
        return DEFAULT.get();
    }
}
