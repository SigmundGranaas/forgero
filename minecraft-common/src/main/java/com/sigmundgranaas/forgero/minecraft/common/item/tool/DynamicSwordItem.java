package com.sigmundgranaas.forgero.minecraft.common.item.tool;

import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.Writer;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class DynamicSwordItem extends SwordItem implements StateItem {
    private final StateProvider DEFAULT;

    public DynamicSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, StateProvider defaultState) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public State defaultState() {
        return DEFAULT.get();
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
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        StateWriter.of(state(itemStack)).write(tooltip, tooltipContext);

        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
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
    public boolean isEffectiveOn(BlockState state) {
        return  state.isOf(Blocks.COBWEB) && isCorrectMiningLevel(state);
    }
}

