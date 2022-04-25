package com.sigmundgranaas.forgero.core.property;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifierImpl;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.pattern.HeadPattern;
import com.sigmundgranaas.forgero.core.pattern.Pattern;
import com.sigmundgranaas.forgero.core.property.attribute.Target;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
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
    public static Supplier<PrimaryMaterial> DIAMOND_PRIMARY = () -> (PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("diamond"));
    public static Supplier<SecondaryMaterial> DIAMOND_SECONDARY = () -> (SecondaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("diamond"));

    public static Supplier<PrimaryMaterial> OAK_PRIMARY = () -> (PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("oak"));
    public static Supplier<SecondaryMaterial> OAK_SECONDARY = () -> (SecondaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("oak"));

    public static Supplier<PrimaryMaterial> NETHERITE_PRIMARY = () -> (PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("netherite"));
    public static Supplier<SecondaryMaterial> NETHERITE_SECONDARY = () -> (SecondaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("netherite"));

    public static Supplier<PrimaryMaterial> IRON_PRIMARY = () -> (PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("iron"));
    public static Supplier<SecondaryMaterial> IRON_SECONDARY = () -> (SecondaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(new ForgeroMaterialIdentifierImpl("iron"));

    public static Supplier<Pattern> HANDLE_PATTERN = () -> ForgeroRegistry.getInstance().patternCollection().getPatterns().stream().filter(pattern -> pattern.getPatternIdentifier().equals("handle_pattern_default")).findFirst().get();
    public static Supplier<HeadPattern> PICKAXEHEAD_PATTERN = () -> (HeadPattern) ForgeroRegistry.getInstance().patternCollection().getPatterns().stream().filter(pattern -> pattern.getPatternIdentifier().equals("pickaxehead_pattern_default")).findFirst().get();
    public static Supplier<HeadPattern> PICKAXEHEAD_PATTERN_PATTERN = () -> (HeadPattern) ForgeroRegistry.getInstance().patternCollection().getPatterns().stream().filter(pattern -> pattern.getPatternIdentifier().equals("pickaxehead_pattern_pattern")).findFirst().get();
    public static Supplier<HeadPattern> PICKAXEHEAD_PATTERN_VEIN = () -> (HeadPattern) ForgeroRegistry.getInstance().patternCollection().getPatterns().stream().filter(pattern -> pattern.getPatternIdentifier().equals("pickaxehead_pattern_vein")).findFirst().get();


    @Test
    void testToolDurability() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(NETHERITE_PRIMARY.get(), PICKAXEHEAD_PATTERN.get());
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_PATTERN.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertTrue(exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.DURABILITY) > 3000);
    }

    @Test
    void testToolMiningSpeed() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(NETHERITE_PRIMARY.get(), PICKAXEHEAD_PATTERN.get());
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_PATTERN.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertEquals(10.0f, exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.MINING_SPEED));
    }

    @Test
    void testToolAttackSpeed() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(NETHERITE_PRIMARY.get(), PICKAXEHEAD_PATTERN.get());
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_PATTERN.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertEquals(-2.8, exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.ATTACK_SPEED), 0.5f);
    }

    @Test
    void testToolAttackDamage() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(NETHERITE_PRIMARY.get(), PICKAXEHEAD_PATTERN.get());
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_PATTERN.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertEquals(6.0, exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.ATTACK_DAMAGE));
    }
}
