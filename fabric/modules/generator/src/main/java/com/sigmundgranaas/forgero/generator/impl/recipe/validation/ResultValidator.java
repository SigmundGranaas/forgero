package com.sigmundgranaas.forgero.generator.impl.recipe.validation;

import static com.sigmundgranaas.forgero.core.Forgero.LOGGER;

import com.google.gson.JsonObject;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ResultValidator {
    public boolean validateResult(JsonObject json) {
        if (!json.has("result")) {
            LOGGER.error("Missing result for recipe");
            return false;
        }

        JsonObject result = json.getAsJsonObject("result");
        
        // Check for standard item result
        if (result.has("item")) {
            Identifier item = new Identifier(result.get("item").getAsString());
            if (!Registries.ITEM.containsId(item)) {
                LOGGER.error("Invalid result item: {}", item);
                return false;
            }
            return true;
        } 
        // Check for liquid result
        else if (result.has("liquid")) {
            try {
                new Identifier(result.get("liquid").getAsString());
                return true;
            } catch (Exception e) {
                LOGGER.error("Invalid liquid identifier in result: {}", e.getMessage());
                return false;
            }
        }
        
        LOGGER.error("Result must contain either 'item' or 'liquid'");
        return false;
    }
}

