package com.sigmundgranaas.forgero.minecraft.common.item.tool;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulParser;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.SoulWriter;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.Writer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

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
        return getDurabilityColor(stack);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        SoulParser.of(itemStack).ifPresent(soul -> new SoulWriter(soul).write(tooltip, tooltipContext));
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
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        soulParticles(stack, world, entity);
    }

    private void soulParticles(ItemStack stack, World world, Entity entity) {
        if (entity instanceof PlayerEntity player && (player.getOffHandStack().isOf(this) || player.getMainHandStack().isOf(this))) {
            Random random = new Random();
            if (random.nextDouble() > 0.99) {
                var vec = player.getHandPosOffset(stack.getItem());
                double z = player.getZ() + vec.z;
                double x = player.getX() + vec.x;
                double y = player.getY() + 1f;
                for (int i = 0; i < 2; i++) {
                    world.addParticle(ParticleTypes.SOUL, x + random.nextGaussian() / 10, y + random.nextGaussian() / 10, z + random.nextGaussian() / 10, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public boolean isEffectiveOn(BlockState state) {
        return state.isOf(Blocks.COBWEB) && isCorrectMiningLevel(state);
    }
}

