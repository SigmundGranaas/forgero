package com.sigmundgranaas.forgero.item.items.tool;

import com.sigmundgranaas.forgero.ForgeroInitializer;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

public class ForgeroSwordItem extends SwordItem implements ForgeroToolItem {

    private final FabricToForgeroToolAdapter toolAdapter = FabricToForgeroToolAdapter.createAdapter();
    private final ForgeroTool tool;

    public ForgeroSwordItem(ToolMaterial toolMaterial, Settings settings, ForgeroTool tool) {
        super(toolMaterial, (int) tool.getAttackDamage(Target.createEmptyTarget()), tool.getAttackSpeed(Target.createEmptyTarget()), settings);

        this.tool = tool;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        ForgeroTool forgeroTool = toolAdapter.getTool(itemStack).orElse(tool);
        forgeroTool.createWeaponDescription(new DescriptionWriter(tooltip));
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.SWORD;
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
        return TagKey.of(Registry.BLOCK_KEY, new Identifier("minecraft", "cobweb"));
    }

    @Override
    public @NotNull ForgeroSwordItem getItem() {
        return this;
    }

    @Override
    protected String getOrCreateTranslationKey() {
        return String.format("item.%s.%s%s%s", ForgeroInitializer.MOD_NAMESPACE, tool.getToolHead().getPrimaryMaterial().getResourceName(), ELEMENT_SEPARATOR, getToolType().toString().toLowerCase(Locale.ROOT));
    }


}
