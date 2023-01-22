package com.sigmundgranaas.forgero.minecraft.common.item.tool;

import com.sigmundgranaas.forgero.core.soul.SoulContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.Writer;
import com.sigmundgranaas.forgero.minecraft.common.soul.SoulHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        StateWriter.of(state(itemStack)).write(tooltip, tooltipContext);
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        var converted = StateConverter.of(stack);
        if (converted.isPresent() && converted.get() instanceof ConstructedTool tool) {
            user.setStackInHand(hand, StateConverter.of(SoulHelper.of(entity, tool)));
        }
        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && StateConverter.of(user.getStackInHand(hand)).orElse(this) instanceof SoulContainer container) {
            Map<String, Integer> map = new HashMap<>(container.getSoul().tracker().blocks().toMap());
            map.putAll(container.getSoul().tracker().mobs().toMap());
            map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered((entry) -> user.sendMessage(Text.literal(String.format("%s: %s", entry.getKey(), entry.getValue()))));
        }
        return super.use(world, user, hand);
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

