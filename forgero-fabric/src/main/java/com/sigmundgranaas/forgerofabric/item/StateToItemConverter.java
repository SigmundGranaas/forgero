package com.sigmundgranaas.forgerofabric.item;

import com.sigmundgranaas.forgero.minecraft.common.item.DefaultStateItem;
import com.sigmundgranaas.forgero.minecraft.common.item.GemItem;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.*;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.state.StateProvider;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class StateToItemConverter {
    private final StateProvider provider;

    public StateToItemConverter(StateProvider provider) {
        this.provider = provider;
    }

    public static StateToItemConverter of(StateProvider provider) {
        return new StateToItemConverter(provider);
    }

    public Item convert() {
        var context = Context.of();
        var state = provider.get();
        int attack_damage = (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE);
        float attack_speed = state.stream().applyAttribute(AttributeType.ATTACK_SPEED);
        if (state.type().test(Type.of("SWORD"), context)) {
            return new DynamicSwordItem(ToolMaterials.WOOD, (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
        } else if (state.type().test(Type.of("PICKAXE"), context)) {
            return new DynamicPickaxeItem(ToolMaterials.WOOD, (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
        } else if (state.type().test(Type.of("AXE"), context)) {
            return new DynamicAxeItem(ToolMaterials.WOOD, attack_damage, attack_speed, getItemSettings(state), () -> state);
        } else if (state.type().test(Type.of("HOE"), context)) {
            return new DynamicHoeItem(ToolMaterials.WOOD, (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
        } else if (state.type().test(Type.of("SHOVEL"), context)) {
            return new DynamicShovelItem(ToolMaterials.WOOD, (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
        } else if (state.type().test(Type.GEM)) {
            return new GemItem(getItemSettings(state), state);
        }
        return defaultStateItem();
    }

    public Identifier id() {
        return new Identifier(provider.get().nameSpace(), provider.get().name());
    }

    private Item defaultStateItem() {
        return new DefaultStateItem(new FabricItemSettings().group(getItemGroup(provider.get())), provider);
    }

    private ItemGroup getItemGroup(State state) {
        if (state.test(Type.TOOL)) {
            return ItemGroup.TOOLS;
        } else if (state.test(Type.WEAPON)) {
            return ItemGroup.COMBAT;
        } else if (state.test(Type.PART)) {
            return ItemGroups.FORGERO_TOOL_PARTS;
        } else if (state.test(Type.SCHEMATIC)) {
            return ItemGroups.FORGERO_SCHEMATICS;
        } else if (state.test(Type.TRINKET)) {
            return ItemGroups.FORGERO_GEMS;
        }
        return ItemGroup.MISC;
    }

    private FabricItemSettings getItemSettings(State state) {
        var settings = new FabricItemSettings();

        settings.group(getItemGroup(state));

        if (state.name().contains("schematic")) {
            settings.recipeRemainder(Registry.ITEM.get(new Identifier(state.identifier())));
        }

        if (state.name().contains("netherite")) {
            settings.fireproof();
        }
        return settings;
    }
}
