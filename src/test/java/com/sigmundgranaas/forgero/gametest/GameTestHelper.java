package com.sigmundgranaas.forgero.gametest;

import com.mojang.authlib.GameProfile;
import com.sigmundgranaas.forgero.core.data.pojo.MaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.implementation.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.adapter.SimpleToolMaterialAdapter;
import com.sigmundgranaas.forgero.item.items.tool.ForgeroPickaxeItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;

import java.util.UUID;

import static com.sigmundgranaas.forgero.core.property.ToolPropertyTest.HANDLE_SCHEMATIC;
import static com.sigmundgranaas.forgero.core.property.ToolPropertyTest.PICKAXEHEAD_SCHEMATIC;

public class GameTestHelper {
    public static ServerPlayerEntity createDummyServerPlayer(TestContext context) {
        ServerPlayerEntity serverPlayer = new ServerPlayerEntity(context.getWorld().getServer(), context.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-serverPlayer"));
        serverPlayer.networkHandler = new ServerPlayNetworkHandler(context.getWorld().getServer(), new ClientConnection(NetworkSide.CLIENTBOUND), serverPlayer);
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
        PrimaryMaterial material = new SimpleDuoMaterial(MaterialPOJO.createDefaultMaterialPOJO());
        return ForgeroToolPartFactory.INSTANCE.createToolPartHeadBuilder(material, PICKAXEHEAD_SCHEMATIC.get()).createToolPart();
    }

    public static ToolPartHandle createDummyToolPartHandle() {
        PrimaryMaterial material = new SimpleDuoMaterial(MaterialPOJO.createDefaultMaterialPOJO());
        return ForgeroToolPartFactory.INSTANCE.createToolPartHandleBuilder(material, HANDLE_SCHEMATIC.get()).createToolPart();
    }

    public static SimpleDuoMaterial createDummyDuoMaterial() {
        return new SimpleDuoMaterial(MaterialPOJO.createDefaultMaterialPOJO());
    }
}
