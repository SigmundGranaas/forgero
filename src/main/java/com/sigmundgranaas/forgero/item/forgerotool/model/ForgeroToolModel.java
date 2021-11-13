package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.google.common.base.Charsets;
import com.mojang.datafixers.util.Pair;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Resource;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeroToolModel implements UnbakedModel, BakedModel, FabricBakedModel {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private final ToolPartModelManager partModels;
    private HashMap<String, FabricBakedModel> BAKED_PART_MODELS = new HashMap<>();
    private Sprite sprite = null;

    public ForgeroToolModel(ToolPartModelManager partModels) {
        this.partModels = partModels;
    }

    public static ModelTransformation loadTransformFromJson(Identifier location) {
        try {
            return JsonUnbakedModel.deserialize(getReaderForResource(location)).getTransformations();
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static Reader getReaderForResource(Identifier location) throws IOException {
        Identifier file = new Identifier(location.getNamespace(), location.getPath() + ".json");
        Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(file);
        return new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
    }

    public static ModelTransformation loadTransformFromJsonString(String json) {
        return JsonUnbakedModel.deserialize(json).getTransformations();
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        FabricBakedModel itemHead = null;
        FabricBakedModel itemHandle = null;
        FabricBakedModel itemBinding = null;
        FabricBakedModel itemModifier = null;
        FabricBakedModel itemUpgrade = null;
        Item item = stack.getItem();
        if (item instanceof ForgeroTool) {
            ForgeroToolPartItem head = ((ForgeroTool) item).getToolHead();
            ForgeroToolPartItem handle = ((ForgeroTool) item).getToolHandle();
            itemHead = partModels.getModel(head.getToolPartTypeAndMaterialLowerCase() + "_toolpart");
            itemHandle = partModels.getModel(handle.getToolPartTypeAndMaterialLowerCase() + "_toolpart");

            NbtCompound nbt = stack.getNbt();
            if (nbt != null) {
                itemBinding = partModels.getModel(nbt.getString("binding"));
            }
        }
        if (itemHead != null || itemHandle != null) {
            assert itemHead != null;
            assert itemHandle != null;
            if (itemBinding != null) {
                itemBinding.emitItemQuads(null, null, context);
            }
            itemHandle.emitItemQuads(null, null, context);
            itemHead.emitItemQuads(null, null, context);
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return null;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("block/cobblestone"));
    }

    @Override
    public ModelTransformation getTransformation() {
        return loadTransformFromJson(new Identifier("minecraft:models/item/handheld"));
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        partModels.bakeToolPartModels(loader);
        return this;
    }
}
