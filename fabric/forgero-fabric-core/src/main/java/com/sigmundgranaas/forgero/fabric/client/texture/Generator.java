package com.sigmundgranaas.forgero.fabric.client.texture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.model.ModelTemplate;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.model.TextureModel;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.fabric.client.ForgeroClient.TEXTURES;


public class Generator {
    public static final RuntimeResourcePack RESOURCE_PACK_CLIENT = RuntimeResourcePack.create("forgero:builtin");

    public static void generate() {
        new Generator().generateModels();
    }

    public void generateModels() {
        JsonArray textures = new JsonArray();
        JsonObject atlas = new JsonObject();

        TEXTURES.values().stream()
                .map(this::texture)
		        .map(Identifier::toString)
		        .distinct()
		        .map(this::textureEntry)
                .forEach(textures::add);
        atlas.add("sources", textures);

        RESOURCE_PACK_CLIENT.addAsset(new Identifier("minecraft:atlases/blocks.json"), atlas.toString().getBytes());
    }

    private JsonObject textureEntry(String texture) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "single");
        entry.addProperty("resource", texture);
        return entry;
    }

    private Identifier texture(ModelTemplate texture) {
	    if(texture instanceof PaletteTemplateModel paletteTemplateModel){
		   return new Identifier(paletteTemplateModel.nameSpace(), "item/" + paletteTemplateModel.name().replace(".png", ""));
	    }else if(texture instanceof TextureModel model){
		    return new Identifier(model.nameSpace(), "item/" + model.name().replace(".png", ""));
	    }
		throw new RuntimeException("Unknown texture type");
    }
}
