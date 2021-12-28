package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.binding;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.AbstractDynamicModel;
import com.sigmundgranaas.forgero.item.implementation.ToolPartItemImpl;
import net.minecraft.client.util.ModelIdentifier;

public class ShovelBindingModel extends AbstractDynamicModel {
    private final static String ELEMENTS = "[{\"name\":\"shovel_bidning\",\"from\":[9,7,7.3],\"to\":[10,11,8.7],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[12,1,5]},\"faces\":{\"north\":{\"uv\":[11,7,12,3],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[11.5,3,12,7],\"texture\":\"#0\"},\"south\":{\"uv\":[11,3,12,7],\"texture\":\"#0\"},\"west\":{\"uv\":[11,3,11.5,7],\"texture\":\"#0\"},\"up\":{\"uv\":[12,3,11,3.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[11,6.5,12,7],\"texture\":\"#0\"}}},{\"name\":\"shovel_bidning\",\"from\":[8,8,7.3],\"to\":[9,10,8.7],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[12,1,5]},\"faces\":{\"north\":{\"uv\":[10,6,11,4],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[10.5,4,11,6],\"texture\":\"#0\"},\"south\":{\"uv\":[10,4,11,6],\"texture\":\"#0\"},\"west\":{\"uv\":[10,4,10.5,6],\"texture\":\"#0\"},\"up\":{\"uv\":[11,4,10,4.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[10,5.5,11,6],\"texture\":\"#0\"}}},{\"name\":\"shovel_bidning\",\"from\":[10,7,7.3],\"to\":[11,10,8.7],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[12,1,5]},\"faces\":{\"north\":{\"uv\":[12,7,13,4],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[12.5,4,13,7],\"texture\":\"#0\"},\"south\":{\"uv\":[12,4,13,7],\"texture\":\"#0\"},\"west\":{\"uv\":[12,4,12.5,7],\"texture\":\"#0\"},\"up\":{\"uv\":[13,4,12,4.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[12,6.5,13,7],\"texture\":\"#0\"}}},{\"name\":\"shovel_bidning\",\"from\":[11,8,7.3],\"to\":[12,9,8.7],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[12,1,5]},\"faces\":{\"north\":{\"uv\":[13,6,14,5],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[13.5,5,14,6],\"texture\":\"#0\"},\"south\":{\"uv\":[13,5,14,6],\"texture\":\"#0\"},\"west\":{\"uv\":[13,5,13.5,6],\"texture\":\"#0\"},\"up\":{\"uv\":[14,5,13,5.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[13,5.5,14,6],\"texture\":\"#0\"}}}]";

    public ShovelBindingModel(ToolPartItemImpl toolpartItem) {
        super(toolpartItem);
    }

    @Override
    public ModelIdentifier getModelIdentifier() {
        return new ModelIdentifier(Forgero.MOD_NAMESPACE, super.itemPartModelIdentifier() + "_shovel", "inventory");
    }

    @Override
    protected JsonArray getElements() {
        return (JsonArray) new JsonParser().parse(ELEMENTS);
    }

    @Override
    protected JsonObject getTextures() {
        JsonObject textures = new JsonObject();
        String texture = super.getTexture();
        textures.addProperty("0", texture);
        textures.addProperty("particle", texture);
        return textures;
    }
}
