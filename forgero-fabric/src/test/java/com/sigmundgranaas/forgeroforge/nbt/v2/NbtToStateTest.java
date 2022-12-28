package com.sigmundgranaas.forgeroforge.nbt.v2;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgeroforge.item.nbt.v2.StateParser;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.resource.PipelineBuilder;
import com.sigmundgranaas.forgeroforge.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Ingredient;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgeroforge.testutil.Materials;
import com.sigmundgranaas.forgeroforge.testutil.Schematics;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgeroforge.nbt.NBTs.PICKAXE_NBT;
import static com.sigmundgranaas.forgeroforge.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgeroforge.testutil.ToolParts.PICKAXE_HEAD;


public class NbtToStateTest {

    public static Optional<State> ingredientSupplier(String id) {
        return switch (id) {
            case "forgero#oak-handle" -> Optional.of(Ingredient.of(HANDLE));
            case "forgero#iron-pickaxe_head" -> Optional.of(Ingredient.of(PICKAXE_HEAD));
            case "forgero#iron" -> Optional.of(Materials.IRON);
            case "forgero#oak" -> Optional.of(Materials.OAK);
            case "forgero#pickaxehead-schematic" -> Optional.of(Schematics.PICKAXE_HEAD_SCHEMATIC);
            case "forgero#handle-schematic" -> Optional.of(Schematics.HANDLE_SCHEMATIC);
            case "forgero#binding-schematic" -> Optional.of(Schematics.BINDING_SCHEMATIC);
            default -> Optional.empty();
        };
    }

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
    void parseInvalidNbt() {
        Assertions.assertEquals(Optional.empty(), StateParser.STATE_PARSER.parse(new NbtCompound()));
    }

    @Test
    void parseSimplePickaxe() {
        Assertions.assertEquals("iron-pickaxe", StateParser.STATE_PARSER.parse(PICKAXE_NBT).map(State::name).orElse("invalid"));
    }

    @Test
    void parseSimplePickaxeWithIngredients() {
        List<State> ingredients = StateParser.STATE_PARSER.parse(PICKAXE_NBT).map(Composite.class::cast).map(Composite::ingredients).orElse(Collections.emptyList());
        Assertions.assertEquals(2, ingredients.size());
        Assertions.assertEquals("oak-handle", ingredients.get(0).name());
        Assertions.assertEquals("iron-pickaxe_head", ingredients.get(1).name());
    }

    @Test()
    void parseSimplePickaxeWithUpgrades() {
        List<State> upgrades = StateParser.STATE_PARSER.parse(PICKAXE_NBT).map(Composite.class::cast).map(Composite::upgrades).orElse(ImmutableList.<State>builder().build());
        //Assertions.assertEquals(1, upgrades.size());
        //Assertions.assertEquals("iron-binding", upgrades.get(0).name());
    }

    @Test
    void assertPropertiesParsed() {
        var pickaxe = StateParser.STATE_PARSER.parse(PICKAXE_NBT).orElseThrow();
        Assertions.assertEquals(0, pickaxe.stream().applyAttribute(AttributeType.ATTACK_DAMAGE));
    }
}