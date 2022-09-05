package com.sigmundgranaas.forgero.nbt.v2;

import com.sigmundgranaas.forgerocore.property.AttributeType;
import com.sigmundgranaas.forgerocore.state.Composite;
import com.sigmundgranaas.forgero.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgero.item.nbt.v2.CompositeParser;
import com.sigmundgranaas.forgero.item.nbt.v2.CompoundEncoder;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.sigmundgranaas.forgero.core.testutil.Tools.IRON_PICKAXE;
import static com.sigmundgranaas.forgero.core.testutil.Upgrades.BINDING;

public class StateNbtConversionTest {
    private static final CompoundEncoder<Composite> encoder = new CompositeEncoder();
    private static final CompositeParser parser = new CompositeParser(NbtToStateTest::ingredientSupplier, (String id) -> Optional.empty());

    @Test
    void encodeCompoundParseCompound() {
        NbtCompound compound = encoder.encode(IRON_PICKAXE);
        var pickaxe = parser.parse(compound).orElseThrow();
        Assertions.assertEquals("iron-pickaxe", pickaxe.name());
    }

    @Test
    void encodeCompoundParseCompoundWithIngredients() {
        NbtCompound compound = encoder.encode(IRON_PICKAXE);
        var pickaxe = parser.parse(compound).orElseThrow();
        Assertions.assertEquals(2, pickaxe.ingredients().size());
        Assertions.assertEquals("oak-handle", pickaxe.ingredients().get(0).name());
        Assertions.assertEquals("iron-pickaxe_head", pickaxe.ingredients().get(1).name());
    }

    @Test
    void encodeCompoundParseCompoundWithUpgrades() {
        NbtCompound compound = encoder.encode(IRON_PICKAXE.upgrade(BINDING));
        var pickaxe = parser.parse(compound).orElseThrow();
        Assertions.assertEquals(1, pickaxe.upgrades().size());
        Assertions.assertEquals("oak-binding", pickaxe.upgrades().get(0).name());
    }

    @Test
    void encodeCompoundParseCompoundWithProperties() {
        NbtCompound compound = encoder.encode(IRON_PICKAXE.upgrade(BINDING));
        var pickaxe = parser.parse(compound).orElseThrow();
        Assertions.assertEquals(13, pickaxe.stream().applyAttribute(AttributeType.ATTACK_DAMAGE));
        Assertions.assertEquals(2000, pickaxe.stream().applyAttribute(AttributeType.DURABILITY));
    }
}
