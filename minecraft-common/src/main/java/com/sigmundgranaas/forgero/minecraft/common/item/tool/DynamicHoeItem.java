package com.sigmundgranaas.forgero.minecraft.common.item.tool;

import com.sigmundgranaas.forgero.core.soul.SoulContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.DynamicAttributeItem;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.Writer;
import com.sigmundgranaas.forgero.minecraft.common.soul.SoulHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicHoeItem extends HoeItem implements DynamicAttributeItem, State, StateItem {
    private final StateProvider DEFAULT;

    public DynamicHoeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, StateProvider defaultState) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.DEFAULT = defaultState;
    }

    @Override
    public boolean isEffectiveOn(BlockState state) {
        return state.isIn(BlockTags.HOE_MINEABLE) && isCorrectMiningLevel(state);
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
    public State defaultState() {
        return DEFAULT.get();
    }
}

