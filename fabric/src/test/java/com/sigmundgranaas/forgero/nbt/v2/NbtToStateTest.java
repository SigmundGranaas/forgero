package com.sigmundgranaas.forgero.nbt.v2;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.item.items.testutil.Schematics;
import com.sigmundgranaas.forgerocore.property.AttributeType;
import com.sigmundgranaas.forgerocore.state.Composite;
import com.sigmundgranaas.forgerocore.state.Ingredient;
import com.sigmundgranaas.forgerocore.state.Upgrade;

import com.sigmundgranaas.forgero.item.items.testutil.Materials;
import com.sigmundgranaas.forgero.item.nbt.v2.CompositeParser;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.item.items.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgero.item.items.testutil.ToolParts.PICKAXE_HEAD;
import static com.sigmundgranaas.forgero.nbt.NBTs.PICKAXE_NBT;


public class NbtToStateTest {
    private static final CompositeParser parser = new CompositeParser(NbtToStateTest::ingredientSupplier, (String id) -> Optional.empty());

    public static Optional<Ingredient> ingredientSupplier(String id) {
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

    @Test
    void parseInvalidNbt() {
        Assertions.assertEquals(Optional.empty(), parser.parse(new NbtCompound()));
    }

    @Test
    void parseSimplePickaxe() {
        Assertions.assertEquals("iron-pickaxe", parser.parse(PICKAXE_NBT).map(Composite::name).orElse("invalid"));
    }

    @Test
    void parseSimplePickaxeWithIngredients() {
        List<Ingredient> ingredients = parser.parse(PICKAXE_NBT).map(Composite::ingredients).orElse(Collections.emptyList());
        Assertions.assertEquals(2, ingredients.size());
        Assertions.assertEquals("oak-handle", ingredients.get(0).name());
        Assertions.assertEquals("iron-pickaxe_head", ingredients.get(1).name());
    }

    @Test
    void parseSimplePickaxeWithUpgrades() {
        List<Upgrade> upgrades = parser.parse(PICKAXE_NBT).map(Composite::upgrades).orElse(ImmutableList.<Upgrade>builder().build());
        Assertions.assertEquals(1, upgrades.size());
        Assertions.assertEquals("iron-binding", upgrades.get(0).name());
    }

    @Test
    void assertPropertiesParsed() {
        var pickaxe = parser.parse(PICKAXE_NBT).orElseThrow();
        Assertions.assertEquals(22, pickaxe.stream().applyAttribute(AttributeType.ATTACK_DAMAGE));
    }
}