package com.sigmundgranaas.forgero.item.forgerotool.tool.item;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;


public class ForgeroShovelItem extends ShovelItem implements ForgeroMiningTool {
    private final ForgeroToolPartItem handle;
    private final ForgeroToolPartItem head;

    public ForgeroShovelItem(ToolMaterial toolMaterial, float f, float g, Settings settings, ForgeroToolPartItem head, ForgeroToolPartItem handle) {
        super(toolMaterial, f, g, settings);
        this.head = head;
        this.handle = handle;
    }

    public ForgeroToolPartItem getHandle() {
        return handle;
    }

    public ForgeroToolPartItem getHead() {
        return head;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {

    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.SHOVEL;
    }

    @Override
    public String getToolTypeLowerCaseString() {
        return getToolType().toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getToolTip() {
        return "Shovel";
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(Forgero.MOD_NAMESPACE, getToolTypeLowerCaseString() + "_" + head.getToolPartTypeAndMaterialLowerCase() + "_" + handle.getToolPartTypeAndMaterialLowerCase());
    }

    @Override
    public ForgeroToolPartItem getToolHead() {
        return getHead();
    }

    @Override
    public ForgeroToolPartItem getToolHandle() {
        return getHandle();
    }

    @Override
    protected String getOrCreateTranslationKey() {
        return String.format("item.%s.%s_%s", Forgero.MOD_NAMESPACE, getHead().getMaterial().toString().toLowerCase(Locale.ROOT), getToolType().toString().toLowerCase(Locale.ROOT));
    }
}
