package com.sigmundgranaas.forgero.item.items.tool;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

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
    public Identifier getIdentifier() {
        return new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString());
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
        return getForgeroTranslatableToolName();
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
    public Tag<Block> getToolTags() {
        return BlockTags.WOOL;
    }

    @Override
    public @NotNull ForgeroAxeItem getItem() {
        return this;
    }

    @Override
    protected String getOrCreateTranslationKey() {
        return String.format("item.%s.%s_%s", ForgeroInitializer.MOD_NAMESPACE, tool.getToolHead().getPrimaryMaterial().getResourceName(), getToolType().toString().toLowerCase(Locale.ROOT));
    }


}
