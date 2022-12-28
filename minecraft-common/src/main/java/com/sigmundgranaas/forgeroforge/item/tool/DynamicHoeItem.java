package com.sigmundgranaas.forgeroforge.item.tool;

import com.sigmundgranaas.forgeroforge.item.DynamicAttributeItem;
import com.sigmundgranaas.forgeroforge.item.StateItem;
import com.sigmundgranaas.forgeroforge.item.tooltip.StateWriter;
import com.sigmundgranaas.forgeroforge.item.tooltip.Writer;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.state.StateProvider;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class DynamicHoeItem extends HoeItem implements DynamicAttributeItem, State, StateItem {
    private final StateProvider DEFAULT;

    public DynamicHoeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, StateProvider defaultState) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public boolean isEffectiveOn(BlockState state) {
        return state.isIn(BlockTags.HOE_MINEABLE);
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
    public State defaultState() {
        return DEFAULT.get();
    }
}

