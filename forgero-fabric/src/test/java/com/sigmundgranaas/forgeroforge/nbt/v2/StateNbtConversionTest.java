package com.sigmundgranaas.forgeroforge.nbt.v2;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgeroforge.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgeroforge.item.nbt.v2.CompositeParser;
import com.sigmundgranaas.forgeroforge.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgeroforge.item.nbt.v2.StateParser;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.resource.PipelineBuilder;
import com.sigmundgranaas.forgeroforge.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgeroforge.testutil.Tools.IRON_PICKAXE;
import static com.sigmundgranaas.forgeroforge.testutil.Upgrades.BINDING;

public class StateNbtConversionTest {
    private static final CompoundEncoder<State> encoder = new CompositeEncoder();
    private static final CompositeParser parser = new CompositeParser(NbtToStateTest::ingredientSupplier);

    @BeforeEach
    void genData() {
        PipelineBuilder
                .builder()
                .register(ForgeroSettings.SETTINGS)
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
        NbtCompound compound = encoder.encode(IRON_PICKAXE);

        var pickaxe = parser.parse(compound).orElseThrow();
        Assertions.assertEquals("iron-pickaxe", pickaxe.name());
    }

    @Test
    void encodeCompoundParseCompoundWithIngredients() {
        NbtCompound compound = encoder.encode(IRON_PICKAXE);
        var pickaxe = parser.parse(compound).map(Composite.class::cast).orElseThrow();
        Assertions.assertEquals(2, pickaxe.ingredients().size());
        Assertions.assertEquals("oak-handle", pickaxe.ingredients().get(0).name());
        Assertions.assertEquals("iron-pickaxe_head", pickaxe.ingredients().get(1).name());
    }

    @Test
    void encodeCompoundParseCompoundWithUpgrades() {
        NbtCompound compound = encoder.encode(IRON_PICKAXE.upgrade(BINDING));
        var pickaxe = StateParser.STATE_PARSER.parse(compound).map(Composite.class::cast).orElseThrow();
        //Assertions.assertEquals(0, pickaxe.upgrades().size());
        //Assertions.assertEquals("oak-binding", pickaxe.upgrades().get(0).name());
    }

    @Test
    void encodeCompoundParseCompoundWithProperties() {
        NbtCompound compound = encoder.encode(IRON_PICKAXE.upgrade(BINDING));
        var pickaxe = parser.parse(compound).map(Composite.class::cast).orElseThrow();
        Assertions.assertEquals(0, pickaxe.stream().applyAttribute(AttributeType.ATTACK_DAMAGE));
        Assertions.assertEquals(0, pickaxe.stream().applyAttribute(AttributeType.DURABILITY));
    }
}
