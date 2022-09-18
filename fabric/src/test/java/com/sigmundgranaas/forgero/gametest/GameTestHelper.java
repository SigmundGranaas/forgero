package com.sigmundgranaas.forgero.gametest;

import com.mojang.authlib.GameProfile;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.implementation.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.adapter.SimpleToolMaterialAdapter;
import com.sigmundgranaas.forgero.item.items.tool.ForgeroPickaxeItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;

import java.util.UUID;

import static com.sigmundgranaas.forgero.property.ToolPropertyTest.HANDLE_SCHEMATIC;
import static com.sigmundgranaas.forgero.property.ToolPropertyTest.PICKAXEHEAD_SCHEMATIC;

public class GameTestHelper {
    public static ServerPlayerEntity createDummyServerPlayer(TestContext context) {
        ServerPlayerEntity serverPlayer = new ServerPlayerEntity(context.getWorld().getServer(), context.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-serverPlayer"), null);
        serverPlayer.networkHandler = new ServerPlayNetworkHandler(context.getWorld().getServer(), new ClientConnection(NetworkSide.SERVERBOUND), serverPlayer);
        return serverPlayer;
    }

    public static ForgeroTool createDummyTool() {
        return ForgeroToolFactory.INSTANCE.createForgeroTool(createDummyToolPartHead(), createDummyToolPartHandle());
    }

    public static ForgeroPickaxeItem createDummyToolItem() {
        SimpleToolMaterialAdapter adapter = new SimpleToolMaterialAdapter(createDummyDuoMaterial());
        ForgeroTool tool = createDummyTool();
        return new ForgeroPickaxeItem(adapter, new FabricItemSettings(), tool);
    }

    public static ToolPartHead createDummyToolPartHead() {
        PrimaryMaterial material = new SimpleDuoMaterial(MaterialPojo.createDefaultMaterialPOJO());
        return ForgeroToolPartFactory.INSTANCE.createToolPartHeadBuilder(material, PICKAXEHEAD_SCHEMATIC.get()).createToolPart();
    }

    public static ToolPartHandle createDummyToolPartHandle() {
        PrimaryMaterial material = new SimpleDuoMaterial(MaterialPojo.createDefaultMaterialPOJO());
        return ForgeroToolPartFactory.INSTANCE.createToolPartHandleBuilder(material, HANDLE_SCHEMATIC.get()).createToolPart();
    }

    public static SimpleDuoMaterial createDummyDuoMaterial() {
        return new SimpleDuoMaterial(MaterialPojo.createDefaultMaterialPOJO());
    }
}
