package com.sigmundgranaas.forgerocore;


import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgerocore.property.AttributeType;
import com.sigmundgranaas.forgerocore.property.Target;
import com.sigmundgranaas.forgerocore.resourceloader.TestResourceLoader;
import com.sigmundgranaas.forgerocore.schematic.HeadSchematic;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgerocore.toolpart.factory.ToolPartBuilder;
import com.sigmundgranaas.forgerocore.toolpart.factory.ToolPartHandleBuilder;
import com.sigmundgranaas.forgerocore.toolpart.factory.ToolPartHeadBuilder;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

public class ToolPropertyTest {
    @BeforeEach
    void initialiseResources() {
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
    }

    public static Supplier<PrimaryMaterial> DIAMOND_PRIMARY = () -> ForgeroRegistry.MATERIAL.getPrimaryMaterial("diamond").get();
    public static Supplier<SecondaryMaterial> DIAMOND_SECONDARY = () -> ForgeroRegistry.MATERIAL.getSecondaryMaterial("diamond").get();

    public static Supplier<PrimaryMaterial> OAK_PRIMARY = () -> ForgeroRegistry.MATERIAL.getPrimaryMaterial("oak").get();
    public static Supplier<SecondaryMaterial> OAK_SECONDARY = () -> ForgeroRegistry.MATERIAL.getSecondaryMaterial("oak").get();

    public static Supplier<PrimaryMaterial> NETHERITE_PRIMARY = () -> ForgeroRegistry.MATERIAL.getPrimaryMaterial("netherite").get();
    public static Supplier<SecondaryMaterial> NETHERITE_SECONDARY = () -> ForgeroRegistry.MATERIAL.getSecondaryMaterial("netherite").get();

    public static Supplier<PrimaryMaterial> IRON_PRIMARY = () -> ForgeroRegistry.MATERIAL.getPrimaryMaterial("iron").get();
    public static Supplier<SecondaryMaterial> IRON_SECONDARY = () -> ForgeroRegistry.MATERIAL.getSecondaryMaterial("iron").get();

    public static Supplier<Schematic> HANDLE_SCHEMATIC = () -> ForgeroRegistry.SCHEMATIC.getResource("handle-schematic").get();
    public static Supplier<HeadSchematic> PICKAXEHEAD_SCHEMATIC = () -> ForgeroRegistry.SCHEMATIC.getHeadSchematic("pickaxe-schematic").get();
    public static Supplier<HeadSchematic> PICKAXEHEAD_SCHEMATIC_PATTERN = () -> ForgeroRegistry.SCHEMATIC.getHeadSchematic("pattern_pickaxe-schematic").get();
    public static Supplier<HeadSchematic> PICKAXEHEAD_SCHEMATIC_VEIN = () -> ForgeroRegistry.SCHEMATIC.getHeadSchematic("vein_pickaxe-schematic").get();


    @Test
    void testToolDurability() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(NETHERITE_PRIMARY.get(), PICKAXEHEAD_SCHEMATIC.get());
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_SCHEMATIC.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertTrue(exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.DURABILITY) > 3000);
    }

    @Test
    void testToolMiningSpeed() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(NETHERITE_PRIMARY.get(), PICKAXEHEAD_SCHEMATIC.get());
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_SCHEMATIC.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertEquals(10.0f, exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.MINING_SPEED), 2);
    }

    @Test
    void testToolAttackSpeed() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(NETHERITE_PRIMARY.get(), PICKAXEHEAD_SCHEMATIC.get());
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_SCHEMATIC.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertEquals(-2.8, exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.ATTACK_SPEED), 0.5f);
    }

    @Test
    void testToolAttackDamage() {
        ToolPartBuilder headBuilder = new ToolPartHeadBuilder(NETHERITE_PRIMARY.get(), PICKAXEHEAD_SCHEMATIC.get());
        headBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ToolPartBuilder handleBuilder = new ToolPartHandleBuilder(OAK_PRIMARY.get(), HANDLE_SCHEMATIC.get());
        handleBuilder.setSecondary(DIAMOND_SECONDARY.get());

        ForgeroTool exampleTool = ForgeroToolFactory.INSTANCE.createForgeroTool((ToolPartHead) headBuilder.createToolPart(), (ToolPartHandle) handleBuilder.createToolPart());

        Assertions.assertEquals(6.0, exampleTool.getPropertyStream().applyAttribute(Target.createEmptyTarget(), AttributeType.ATTACK_DAMAGE));
    }
}
