package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.soul.SoulContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.Upgradeable;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedSchematicPart;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.*;

public class CompositeEncoder implements CompoundEncoder<State> {
    private final IdentifiableEncoder identifiableEncoder;
    private final StateEncoder stateEncoder;

    public CompositeEncoder() {
        this.stateEncoder = new StateEncoder();
        this.identifiableEncoder = new IdentifiableEncoder();
    }

    public static NbtList encodeConditions(Conditional<?> conditional) {
        NbtList list = new NbtList();
        conditional.namedConditions().stream()
                .map(NamedCondition::identifier)
                .map(NbtString::of)
                .forEach(list::add);
        return list;
    }

    @Override
    public NbtCompound encode(State element) {
        var compound = identifiableEncoder.encode(element);
        compound.putString(STATE_TYPE_IDENTIFIER, COMPOSITE_IDENTIFIER);
        compound.putString(TYPE_IDENTIFIER, element.type().typeName());
        if (element instanceof SoulContainer soulContainer) {
            compound.put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(soulContainer.getSoul()));
        }

        if (element instanceof ConstructedTool || element instanceof Constructed constructed && ConstructedTool.ToolBuilder.builder(constructed.parts()).isPresent()) {
            compound.putString(COMPOSITE_TYPE, TOOL_IDENTIFIER);
        }

        if (element instanceof ConstructedSchematicPart || element instanceof Constructed constructed && ConstructedSchematicPart.SchematicPartBuilder.builder(constructed.parts()).isPresent()) {
            compound.putString(COMPOSITE_TYPE, SCHEMATIC_PART_IDENTIFIER);
        }

        if (element instanceof Conditional<?> conditional) {
            compound.put(CONDITIONS_IDENTIFIER, encodeConditions(conditional));
        }

        if (element instanceof Constructed constructed) {
            var ingredients = new NbtList();
            constructed.parts().stream().map(stateEncoder::encode).forEach(ingredients::add);
            compound.put(INGREDIENTS_IDENTIFIER, ingredients);
        }
        if (element instanceof Upgradeable<?> upgradeable) {
            var upgrades = new NbtList();
            upgradeable.upgrades().stream().map(stateEncoder::encode).forEach(upgrades::add);
            compound.put(UPGRADES_IDENTIFIER, upgrades);
        }
        return compound;
    }
}
