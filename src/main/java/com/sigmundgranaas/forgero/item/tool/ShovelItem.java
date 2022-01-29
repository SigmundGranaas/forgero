package com.sigmundgranaas.forgero.item.tool;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;


public class ShovelItem extends net.minecraft.item.ShovelItem implements ForgeroToolItem {
    private final ForgeroTool tool;

    public ShovelItem(ToolMaterial toolMaterial, float f, float g, Settings settings, ForgeroTool tool) {
        super(toolMaterial, f, g, settings);
        this.tool = tool;
    }


    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        ForgeroTool forgeroTool = FabricToForgeroToolAdapter.createAdapter().getTool(itemStack).orElse(tool);
        forgeroTool.createToolDescription(new DescriptionWriter(tooltip));
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
    public Identifier getIdentifier() {
        return new Identifier(Forgero.MOD_NAMESPACE, tool.getToolIdentifierString());
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
    protected String getOrCreateTranslationKey() {
        return String.format("item.%s.%s_%s", Forgero.MOD_NAMESPACE, getHead().getPrimaryMaterial().getName(), getToolType().toString().toLowerCase(Locale.ROOT));
    }
}
