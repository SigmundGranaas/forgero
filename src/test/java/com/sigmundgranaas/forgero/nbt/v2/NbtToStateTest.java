package com.sigmundgranaas.forgero.nbt.v2;

import com.sigmundgranaas.forgero.item.items.testutil.Materials;
import com.sigmundgranaas.forgero.item.items.testutil.ToolParts;
import com.sigmundgranaas.forgero.item.nbt.v2.CompositeParser;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class NbtToStateTest {
    private static final CompositeParser parser = new CompositeParser((String id) -> Optional.of(Materials.IRON), (String id) -> Optional.of(ToolParts.PICKAXE_HEAD), (String id) -> Optional.empty());

    @Test
    void parseInvalidNbt() {
        Assertions.assertEquals(Optional.empty(), parser.parse(new NbtCompound()));
    }
}
