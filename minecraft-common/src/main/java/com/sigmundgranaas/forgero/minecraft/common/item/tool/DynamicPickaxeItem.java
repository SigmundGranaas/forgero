package com.sigmundgranaas.forgero.minecraft.common.item.tool;

import com.sigmundgranaas.forgero.minecraft.common.entity.EnderTeleportationEntity;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.Writer;
import com.sigmundgranaas.forgero.minecraft.common.item.DynamicAttributeItem;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class DynamicPickaxeItem extends PickaxeItem implements DynamicAttributeItem, State, StateItem {
    private final StateProvider DEFAULT;

    public DynamicPickaxeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, StateProvider defaultState) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public boolean isEffectiveOn(BlockState state) {
        return state.isIn(BlockTags.PICKAXE_MINEABLE) && isCorrectMiningLevel(state);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        user.getItemCooldownManager().set(this, 20);
        if (!world.isClient) {
            EnderTeleportationEntity teleportationEntity = new EnderTeleportationEntity(world, user, 5000);
            teleportationEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1F, 1.0F);
            world.spawnEntity(teleportationEntity);

        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));

        return TypedActionResult.success(itemStack, world.isClient());

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

