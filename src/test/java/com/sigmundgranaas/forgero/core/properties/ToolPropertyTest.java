package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.properties.attribute.Target;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartBuilder;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartHandleBuilder;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartHeadBuilder;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

public class ToolPropertyTest {
    static Supplier<PrimaryMaterial> DIAMOND_PRIMARY = () -> (PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("diamond"));
    static Supplier<SecondaryMaterial> DIAMOND_SECONDARY = () -> (SecondaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("diamond"));

    static Supplier<PrimaryMaterial> OAK_PRIMARY = () -> (PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("oak"));
    static Supplier<SecondaryMaterial> OAK_SECONDARY = () -> (SecondaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("oak"));

    static Supplier<PrimaryMaterial> NETHERITE_PRIMARY = () -> (PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("netherite"));
    static Supplier<SecondaryMaterial> NETHERITE_SECONDARY = () -> (SecondaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("netherite"));

    static Supplier<PrimaryMaterial> IRON_PRIMARY = () -> (PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("iron"));
    static Supplier<SecondaryMaterial> IRON_SECONDARY = () -> (SecondaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("iron"));

    @Test
    void testToolDurability() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(NETHERITE_PRIMARY.get(), ForgeroToolTypes.PICKAXE);
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertTrue(exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.DURABILITY) > 3000);
    }
}
