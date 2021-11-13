package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;

/**
 * An interface used to create models which should be rendered dynamically next to other models.
 */
public interface DynamicModel extends UnbakedModel {


    /**
     * Method for return a Json representation of the model which has been created
     *
     * @return JsonModel
     */
    String BuildJsonModel();

    /**
     * Method for creating an Unbaked version of the Model, which is ready to be used in Minecraft's
     * ModeLoader.
     *
     * @return An JsonUnbaked model based on the JsonModel
     */
    JsonUnbakedModel buildUnbakedJsonModel();

    /**
     * Method for creating an Identifier which corresponds to models being used to render parts of a tool
     *
     * @return Unique identifier
     */
    String itemPartModelIdentifier();

    /**
     * @return Identifier used to find cached versions of the model
     */
    ModelIdentifier getModelIdentifier();
}
