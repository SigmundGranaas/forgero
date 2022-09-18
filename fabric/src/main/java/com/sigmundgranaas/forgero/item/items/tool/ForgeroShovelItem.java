package com.sigmundgranaas.forgero.item.items.tool;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.property.Target;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;


public class ForgeroShovelItem extends ShovelItem implements ForgeroToolItem {

    private final ForgeroTool tool;
    private final FabricToForgeroToolAdapter toolAdapter = FabricToForgeroToolAdapter.createAdapter();


    public ForgeroShovelItem(ToolMaterial toolMaterial, Settings settings, ForgeroTool tool) {
        super(toolMaterial, (int) tool.getAttackDamage(Target.createEmptyTarget()), tool.getAttackSpeed(Target.createEmptyTarget()), settings);
        this.tool = tool;
    }


    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        ForgeroTool forgeroTool = FabricToForgeroToolAdapter.createAdapter().getTool(itemStack).orElse(tool);
        forgeroTool.createToolDescription(new DescriptionWriter(tooltip));
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.SHOVEL;
    }

    @Override
    public ForgeroTool getTool() {
        return tool;
    }


    @Override
    public Text getName() {
        return getForgeroTranslatableToolName();
    }


    @Override
    public Text getName(ItemStack stack) {
        return getForgeroTranslatableToolName(getToolAdapter().getTool(stack).orElse(getTool()));
    }

    @Override
    public ToolPartHead getHead() {
        return tool.getToolHead();
    }

    @Override
    public ToolPartHandle getHandle() {
        return tool.getToolHandle();
    }

    @Override
    public FabricToForgeroToolAdapter getToolAdapter() {
        return toolAdapter;
    }

    @Override
    public TagKey<Block> getToolTags() {
        return BlockTags.SHOVEL_MINEABLE;
    }

    @Override
    public @NotNull ForgeroShovelItem getItem() {
        return this;
    }

    @Override
    protected String getOrCreateTranslationKey() {
        return String.format("item.%s.%s%s%s", ForgeroInitializer.MOD_NAMESPACE, getHead().getPrimaryMaterial().getResourceName(), ELEMENT_SEPARATOR, getToolType().toString().toLowerCase(Locale.ROOT));
    }


}
