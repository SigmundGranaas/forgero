package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ToolPartStrategyFactory;
import com.sigmundgranaas.forgero.core.tool.toolpart.head.PickaxeHead;
import com.sigmundgranaas.forgero.core.tool.toolpart.head.ToolPartHeadStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PickaxeHeadTest {
    public static PickaxeHead createDefaultPickaxeHead() {
        ToolPartHeadStrategy strategy = ToolPartStrategyFactory.createToolPartHeadStrategy(ForgeroToolTypes.PICKAXE, (PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.IRON_IDENTIFIER_STRING)));

        return new PickaxeHead(strategy);
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