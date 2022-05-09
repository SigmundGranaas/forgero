package com.sigmundgranaas.forgero.item.items.tool;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.property.attribute.Target;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;


public class ForgeroHoeItem extends HoeItem implements ForgeroToolItem {

    private final ForgeroTool tool;
    private final FabricToForgeroToolAdapter toolAdapter = FabricToForgeroToolAdapter.createAdapter();


    public ForgeroHoeItem(ToolMaterial toolMaterial, Settings settings, ForgeroTool tool) {
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
        return ForgeroToolTypes.HOE;
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
        return getForgeroTranslatableToolName();
    }


    @Override
    public Identifier getIdentifier() {
        return new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString());
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
        return BlockTags.HOE_MINEABLE;
    }

    @Override
    protected String getOrCreateTranslationKey() {
        return String.format("item.%s.%s_%s", ForgeroInitializer.MOD_NAMESPACE, getHead().getPrimaryMaterial().getName(), getToolType().toString().toLowerCase(Locale.ROOT));
    }


}
