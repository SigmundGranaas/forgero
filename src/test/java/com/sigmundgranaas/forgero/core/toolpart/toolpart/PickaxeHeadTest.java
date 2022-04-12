package com.sigmundgranaas.forgero.core.toolpart.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.head.HeadState;
import com.sigmundgranaas.forgero.core.toolpart.head.PickaxeHead;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PickaxeHeadTest {
    public static PickaxeHead createDefaultPickaxeHead() {
        HeadState state = new HeadState((PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING)), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), pattern);
        return new PickaxeHead(state);
    }


    @Test
    void getToolTypeName() {
        Assertions.assertEquals("pickaxe", createDefaultPickaxeHead().getToolTypeName());
    }

    @Test
    void getToolPartName() {
        Assertions.assertEquals("pickaxehead", createDefaultPickaxeHead().getToolPartName());
    }
}