package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.Constants;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ToolPartStrategyFactory;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.HandleStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HandleTest {
    public static Handle createDefaultToolPartHandle() {
        HandleStrategy strategy = ToolPartStrategyFactory.createToolPartHandleStrategy((PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(new ForgeroMaterialIdentifierImpl(Constants.OAK_IDENTIFIER_STRING)));
        return new Handle(strategy);
    }

    @Test
    void getToolPartName() {
        ForgeroToolPart referenceToolPart = createDefaultToolPartHandle();
        Assertions.assertEquals("handle", referenceToolPart.getToolPartName());
    }
}