package com.sigmundgranaas.forgero.item.items.tool;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgerocore.property.Target;
import com.sigmundgranaas.forgerocore.resource.ForgeroResourceType;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static com.sigmundgranaas.forgerocore.identifier.Common.ELEMENT_SEPARATOR;

public class ForgeroAxeItem extends AxeItem implements ForgeroToolItem {

    private final FabricToForgeroToolAdapter toolAdapter = FabricToForgeroToolAdapter.createAdapter();
    private final ForgeroTool tool;

    public ForgeroAxeItem(ToolMaterial toolMaterial, Settings settings, ForgeroTool tool) {
        super(toolMaterial, (int) tool.getAttackDamage(Target.createEmptyTarget()), tool.getAttackSpeed(Target.createEmptyTarget()), settings);

        this.tool = tool;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        ForgeroTool forgeroTool = toolAdapter.getTool(itemStack).orElse(tool);
        forgeroTool.createToolDescription(new DescriptionWriter(tooltip));
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
    }


    @Override
    public Text getName() {
        return getForgeroTranslatableToolName();
    }

    @Override
    public ForgeroResourceType getResourceType() {
        return ForgeroResourceType.TOOL;
    }


    @Override
    public Text getName(ItemStack stack) {
        return getForgeroTranslatableToolName(getToolAdapter().getTool(stack).orElse(getTool()));
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.AXE;
    }

    @Override
    public ForgeroTool getTool() {
        return tool;
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
        return BlockTags.AXE_MINEABLE;
    }

    @Override
    public @NotNull ForgeroAxeItem getItem() {
        return this;
    }

    @Override
    protected String getOrCreateTranslationKey() {
        return String.format("item.%s.%s%s%s", ForgeroInitializer.MOD_NAMESPACE, tool.getToolHead().getPrimaryMaterial().getResourceName(), ELEMENT_SEPARATOR, getToolType().toString().toLowerCase(Locale.ROOT));
    }
}
