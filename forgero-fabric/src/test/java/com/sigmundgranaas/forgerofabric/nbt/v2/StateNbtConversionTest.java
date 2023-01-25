package com.sigmundgranaas.forgerofabric.nbt.v2;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedComposite;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedState;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompositeParser;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.StateParser;
import com.sigmundgranaas.forgerofabric.testutil.*;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Set;

public class StateNbtConversionTest {
    private static final CompoundEncoder<State> encoder = new CompositeEncoder();
    private static final CompositeParser parser = new CompositeParser(NbtToStateTest::ingredientSupplier);

    @BeforeEach
    void genData() {
        PipelineBuilder
                .builder()
                .register(FabricPackFinder.supplier())
                .state(ForgeroStateRegistry.stateListener())
                .state(ForgeroStateRegistry.compositeListener())
                .inflated(ForgeroStateRegistry.constructListener())
                .inflated(ForgeroStateRegistry.containerListener())
                .recipes(ForgeroStateRegistry.recipeListener())
                .build()
                .execute();
    }

    @Test
    void encodeCompoundParseCompound() {
        NbtCompound compound = encoder.encode(Tools.IRON_PICKAXE);

        var pickaxe = parser.parse(compound).orElseThrow();
        Assertions.assertEquals("iron-pickaxe", pickaxe.name());
    }

    @Test
    void encodeCompoundParseCompoundWithIngredients() {
        NbtCompound compound = encoder.encode(Tools.IRON_PICKAXE);
        var pickaxe = parser.parse(compound).map(ConstructedState.class::cast).orElseThrow();
        Assertions.assertEquals(2, pickaxe.parts().size());
        Assertions.assertEquals("oak-handle", pickaxe.parts().get(1).name());
        Assertions.assertEquals("iron-pickaxe_head", pickaxe.parts().get(0).name());
    }

    @Test
    void encodeCompoundParseCompoundWithUpgrades() {
        NbtCompound compound = encoder.encode(Tools.IRON_PICKAXE.upgrade(Upgrades.BINDING));
        var pickaxe = StateParser.STATE_PARSER.parse(compound).map(ConstructedState.class::cast).orElseThrow();
        //Assertions.assertEquals(0, pickaxe.upgrades().size());
        //Assertions.assertEquals("oak-binding", pickaxe.upgrades().get(0).name());
    }

    @Test
    void encodeCompoundParseCompoundWithProperties() {
        NbtCompound compound = encoder.encode(Tools.IRON_PICKAXE.upgrade(Upgrades.BINDING));
        var pickaxe = parser.parse(compound).map(ConstructedState.class::cast).orElseThrow();
        Assertions.assertEquals(0, pickaxe.stream().applyAttribute(AttributeType.ATTACK_DAMAGE));
        Assertions.assertEquals(0, pickaxe.stream().applyAttribute(AttributeType.DURABILITY));
    }

    @Test
    void encodeStateIntoNewToolFormat() {
        State head = ToolParts.PICKAXE_HEAD.upgrade(Materials.IRON);
        State handle = ToolParts.HANDLE.upgrade(Materials.IRON);
        ArrayList<Slot> slots = new ArrayList<>();
        slots.add(new EmptySlot(0, Types.BINDING, "", Set.of(Category.ALL)));

        State tool = ConstructedComposite.ConstructBuilder.builder()
                .addIngredient(head)
                .addIngredient(handle)
                .addSlotContainer(new SlotContainer(slots))
                .addUpgrade(ToolParts.OAK_BINDING)
                .id("forgero:iron-pickaxe")
                .build();
        NbtCompound compound = encoder.encode(tool);
        var pickaxe = parser.parse(compound);

        Assertions.assertTrue(pickaxe.isPresent() && pickaxe.get() instanceof ConstructedTool constructedTool && constructedTool.parts().size() == 2);
    }
}
