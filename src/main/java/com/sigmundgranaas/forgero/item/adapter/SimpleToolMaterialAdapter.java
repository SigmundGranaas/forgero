package com.sigmundgranaas.forgero.item.adapter;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ModelContainerPojo;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ModelPojo;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.texture.TextureModelContainerImpl;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.head.HeadState;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

import java.util.Collections;
import java.util.List;

public class SimpleToolMaterialAdapter implements ToolMaterial {
    private final HeadState state;

    public SimpleToolMaterialAdapter(PrimaryMaterial material) {
        this.state = new HeadState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), new Schematic(ForgeroToolPartTypes.HANDLE, "default", Collections.emptyList(), TextureModelContainerImpl.createContainer(List.of(createModelPojo())).get(), 1, false));

    }

    private ModelContainerPojo createModelPojo() {
        var pojo = new ModelContainerPojo();
        pojo.model = new ModelPojo();
        pojo.id = "DEFAULT";
        pojo.model.primary = "DEFAULT";
        pojo.model.secondary = "DEFAULT";
        pojo.model.gem = "DEFAULT";
        return pojo;
    }

    @Override
    public int getDurability() {
        return (int) Property.stream(state.getProperties(Target.createEmptyTarget())).applyAttribute(Target.createEmptyTarget(), AttributeType.DURABILITY);
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return Property.stream(state.getProperties(Target.createEmptyTarget())).applyAttribute(Target.createEmptyTarget(), AttributeType.MINING_SPEED);
    }

    @Override
    public float getAttackDamage() {
        return Property.stream(state.getProperties(Target.createEmptyTarget())).applyAttribute(Target.createEmptyTarget(), AttributeType.ATTACK_DAMAGE);
    }

    @Override
    public int getMiningLevel() {
        return (int) Property.stream(state.getProperties(Target.createEmptyTarget())).applyAttribute(Target.createEmptyTarget(), AttributeType.MINING_LEVEL);
    }

    @Override
    public int getEnchantability() {
        return 10;
    }

    @Override
    public Ingredient getRepairIngredient() {
        JsonObject ingredient = new JsonObject();
        if (state.getPrimaryMaterial().getIngredient().tag == null) {
            ingredient.addProperty("item", state.getPrimaryMaterial().getIngredient().item);
        } else {
            ingredient.addProperty("tag", state.getPrimaryMaterial().getIngredient().tag);
        }
        return Ingredient.fromJson(ingredient);
    }
}
