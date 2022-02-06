package com.sigmundgranaas.forgero.item.items;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GemItem extends Item {
    private final Gem gem;

    public GemItem(Settings settings, Gem gem) {
        super(settings);
        this.gem = gem;
    }

    public Gem getGem() {
        return gem;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Gem newGem = NBTFactory.INSTANCE.createGemFromNbt(stack.getOrCreateNbt());
        newGem.createToolPartDescription(new DescriptionWriter(tooltip));
    }
}
