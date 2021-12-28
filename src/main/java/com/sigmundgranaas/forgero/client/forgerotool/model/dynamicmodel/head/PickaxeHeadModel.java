package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.head;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.AbstractDynamicModel;
import com.sigmundgranaas.forgero.item.implementation.ToolPartItemImpl;

public class PickaxeHeadModel extends AbstractDynamicModel {
    private static final String ELEMENTS = "[{\"name\":\"pickaxe_head_base\",\"from\":[11,12,7.4],\"to\":[12,13,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[11,4,12,3],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[11.5,3,12,4],\"texture\":\"#0\"},\"south\":{\"uv\":[11,3,12,4],\"texture\":\"#0\"},\"west\":{\"uv\":[11,3,11.5,4],\"texture\":\"#0\"},\"up\":{\"uv\":[12,3,11,3.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[11,3.5,12,4],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[10,11,7.4],\"to\":[11,12,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[11,4,12,3],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[11.5,3,12,4],\"texture\":\"#0\"},\"south\":{\"uv\":[10,4,11,5],\"texture\":\"#0\"},\"west\":{\"uv\":[11,3,11.5,4],\"texture\":\"#0\"},\"up\":{\"uv\":[12,3,11,3.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[11,3.5,12,4],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[12,9,7.4],\"to\":[13,10,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[11,4,12,3],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[11.5,3,12,4],\"texture\":\"#0\"},\"south\":{\"uv\":[12,6,13,7],\"texture\":\"#0\"},\"west\":{\"uv\":[11,3,11.5,4],\"texture\":\"#0\"},\"up\":{\"uv\":[12,3,11,3.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[11,3.5,12,4],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[13,10,7.4],\"to\":[14,11,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[11,4,12,3],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[11.5,3,12,4],\"texture\":\"#0\"},\"south\":{\"uv\":[13,5,14,6],\"texture\":\"#0\"},\"west\":{\"uv\":[11,3,11.5,4],\"texture\":\"#0\"},\"up\":{\"uv\":[12,3,11,3.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[11,3.5,12,4],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[13,4,7.4],\"to\":[13.5,4.5,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[13,12,13.5,11.5],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[13,11.5,13.5,12],\"texture\":\"#0\"},\"south\":{\"uv\":[13,11.5,13.5,12],\"texture\":\"#0\"},\"west\":{\"uv\":[13,11.5,13.5,12],\"texture\":\"#0\"},\"up\":{\"uv\":[13.5,11.5,13,12],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[13,11.5,13.5,12],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[6.5,12,7.4],\"to\":[10.5,14,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[6.5,4,10.5,2],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[10,2,10.5,4.5],\"texture\":\"#0\"},\"south\":{\"uv\":[6.5,2,10.5,4],\"texture\":\"#0\"},\"west\":{\"uv\":[6.5,2,7,4.5],\"texture\":\"#0\"},\"up\":{\"uv\":[10.5,2,6.5,2.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[6.5,4,10.5,4.5],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[6.5,11.5,7.4],\"to\":[10,12,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[6.5,4.5,10.5,4],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[10,2,10.5,4.5],\"texture\":\"#0\"},\"south\":{\"uv\":[6.5,3.5,10,4],\"texture\":\"#0\"},\"west\":{\"uv\":[6.5,2,7,4.5],\"texture\":\"#0\"},\"up\":{\"uv\":[10.5,2,6.5,2.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[6.5,4,10.5,4.5],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[6,11.5,7.4],\"to\":[6.5,13.5,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[6,4.5,6.5,2.5],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[6,2.5,6.5,4.5],\"texture\":\"#0\"},\"south\":{\"uv\":[6,2.5,6.5,4.5],\"texture\":\"#0\"},\"west\":{\"uv\":[6,2.5,6.5,4.5],\"texture\":\"#0\"},\"up\":{\"uv\":[6.5,2.5,6,3],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[6,4,6.5,4.5],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[10.5,12,7.4],\"to\":[11,13.5,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[10.5,5,11,2.5],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[10.5,2.5,11,5],\"texture\":\"#0\"},\"south\":{\"uv\":[10.5,2.5,11,4],\"texture\":\"#0\"},\"west\":{\"uv\":[10.5,2.5,11,5],\"texture\":\"#0\"},\"up\":{\"uv\":[11,2.5,10.5,3],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[10.5,4.5,11,5],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[5.5,11.5,7.4],\"to\":[6,13,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[5.5,4.5,6,3],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[5.5,3,6,4.5],\"texture\":\"#0\"},\"south\":{\"uv\":[5.5,3,6,4.5],\"texture\":\"#0\"},\"west\":{\"uv\":[5.5,3,6,4.5],\"texture\":\"#0\"},\"up\":{\"uv\":[6,3,5.5,3.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[5.5,4,6,4.5],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[5,11.5,7.4],\"to\":[5.5,12,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[5,4,5.5,3.5],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[5,3.5,5.5,4],\"texture\":\"#0\"},\"south\":{\"uv\":[5,3.5,5.5,4],\"texture\":\"#0\"},\"west\":{\"uv\":[5,3.5,5.5,4],\"texture\":\"#0\"},\"up\":{\"uv\":[5.5,3.5,5,4],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[5,3.5,5.5,4],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[9,11,7.4],\"to\":[10,11.5,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[9,5,10.5,4.5],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[10,4.5,10.5,5],\"texture\":\"#0\"},\"south\":{\"uv\":[9,4.5,10,5],\"texture\":\"#0\"},\"west\":{\"uv\":[9,4.5,9.5,5],\"texture\":\"#0\"},\"up\":{\"uv\":[10.5,4.5,9,5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[9,4.5,10.5,5],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[13,4.5,7.4],\"to\":[14,10,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[13,11.5,14,6],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[13.5,5,14,11.5],\"texture\":\"#0\"},\"south\":{\"uv\":[13,6,14,11.5],\"texture\":\"#0\"},\"west\":{\"uv\":[13,5,13.5,11.5],\"texture\":\"#0\"},\"up\":{\"uv\":[14,5,13,5.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[13,11,14,11.5],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[12,8,7.4],\"to\":[13,9,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[12,8,13,6],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[12.5,6,13,8],\"texture\":\"#0\"},\"south\":{\"uv\":[12,7,13,8],\"texture\":\"#0\"},\"west\":{\"uv\":[12,6,12.5,8],\"texture\":\"#0\"},\"up\":{\"uv\":[13,6,12,6.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[12,7.5,13,8],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[14,5,7.4],\"to\":[14.5,10,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[14,11,14.5,6],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[14,6,14.5,11],\"texture\":\"#0\"},\"south\":{\"uv\":[14,6,14.5,11],\"texture\":\"#0\"},\"west\":{\"uv\":[14,6,14.5,11],\"texture\":\"#0\"},\"up\":{\"uv\":[14.5,6,14,6.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[14,10.5,14.5,11],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[14.5,5.5,7.4],\"to\":[15,9.5,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[14.5,10.5,15,6.5],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[14.5,6.5,15,10.5],\"texture\":\"#0\"},\"south\":{\"uv\":[14.5,6.5,15,10.5],\"texture\":\"#0\"},\"west\":{\"uv\":[14.5,6.5,15,10.5],\"texture\":\"#0\"},\"up\":{\"uv\":[15,6.5,14.5,7],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[14.5,10,15,10.5],\"texture\":\"#0\"}}},{\"name\":\"pickaxe_head_base\",\"from\":[12.5,4.5,7.4],\"to\":[13,8,8.6],\"rotation\":{\"angle\":0,\"axis\":\"y\",\"origin\":[-8,0,-20.5]},\"faces\":{\"north\":{\"uv\":[12.5,11.5,13,8],\"rotation\":180,\"texture\":\"#0\"},\"east\":{\"uv\":[12.5,8,13,11.5],\"texture\":\"#0\"},\"south\":{\"uv\":[12.5,8,13,11.5],\"texture\":\"#0\"},\"west\":{\"uv\":[12.5,8,13,11.5],\"texture\":\"#0\"},\"up\":{\"uv\":[13,8,12.5,8.5],\"rotation\":180,\"texture\":\"#0\"},\"down\":{\"uv\":[12.5,11,13,11.5],\"texture\":\"#0\"}}}]";

    public PickaxeHeadModel(ToolPartItemImpl toolpart) {
        super(toolpart);
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
