package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.item.tool.*;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class StateToItemConverter {
    private final State state;

    public StateToItemConverter(State state) {
        this.state = state;
    }

    public static StateToItemConverter of(State state) {
        return new StateToItemConverter(state);
    }

    public Item convert() {
        var context = Context.of();
        int attack_damage = (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE);
        float attack_speed = state.stream().applyAttribute(AttributeType.ATTACK_SPEED);
        if (state.type().test(Type.of("SWORD"), context)) {
            return new DynamicSwordItem(ToolMaterials.WOOD, (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), state);
        } else if (state.type().test(Type.of("PICKAXE"), context)) {
            return new DynamicPickaxeItem(ToolMaterials.WOOD, (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), state);
        } else if (state.type().test(Type.of("AXE"), context)) {
            return new DynamicAxeItem(ToolMaterials.WOOD, attack_damage, attack_speed, getItemSettings(state), state);
        } else if (state.type().test(Type.of("HOE"), context)) {
            return new DynamicHoeItem(ToolMaterials.WOOD, (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), state);
        } else if (state.type().test(Type.of("SHOVEL"), context)) {
            return new DynamicShovelItem(ToolMaterials.WOOD, (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), state);
        } else if (state.type().test(Type.GEM)) {
            return new GemItem(getItemSettings(state), state);
        }
        return defaultStateItem();
    }

    public Identifier id() {
        return new Identifier(state.nameSpace(), state.name());
    }

    private Item defaultStateItem() {
        var item = new DefaultStateItem(new FabricItemSettings(), state);
        ItemGroupEvents.modifyEntriesEvent(getItemGroup(state)).register(entries -> entries.add(item));
        return item;
    }

    public ItemGroup getItemGroup(State state) {
        if (state.test(Type.TOOL)) {
            return ItemGroups.FORGERO_TOOLS;
        } else if (state.test(Type.WEAPON)) {
            return ItemGroups.FORGERO_WEAPONS;
        } else if (state.test(Type.PART)) {
            return ItemGroups.FORGERO_TOOL_PARTS;
        } else if (state.test(Type.SCHEMATIC)) {
            return ItemGroups.FORGERO_SCHEMATICS;
        } else if (state.test(Type.TRINKET)) {
            return ItemGroups.FORGERO_GEMS;
        }
        return net.minecraft.item.ItemGroups.INGREDIENTS;
    }

    private FabricItemSettings getItemSettings(State state) {
        var settings = new FabricItemSettings();

        if (state.name().contains("schematic")) {
            settings.recipeRemainder(Registries.ITEM.get(new Identifier(state.identifier())));
        }

        if (state.name().contains("netherite")) {
            settings.fireproof();
        }
        return settings;
    }
}
