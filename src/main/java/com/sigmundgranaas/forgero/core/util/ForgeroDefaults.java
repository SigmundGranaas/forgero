package com.sigmundgranaas.forgero.core.util;

import com.sigmundgranaas.forgero.core.data.v1.pojo.IngredientPojo;
import com.sigmundgranaas.forgero.core.data.v1.pojo.MaterialPojo;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.MaterialType;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.handle.HandleState;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.state.AbstractToolPartState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface ForgeroDefaults {
    static PrimaryMaterial getDefaultPrimaryMaterial() {
        return new PrimaryMaterial() {
            @Override
            public PrimaryMaterial getResource() {
                return this;
            }

            @Override
            public String getConstructIdentifier() {
                return getStringIdentifier();
            }

            @Override
            public @NotNull List<Property> getProperties(Target target) {
                return Collections.emptyList();
            }

            @Override
            public int getRarity() {
                return 0;
            }

            @Override
            public String getStringIdentifier() {
                return "DEFAULT";
            }

            @Override
            public @NotNull String getResourceName() {
                return "DEFAULT";
            }

            @Override
            public MaterialType getType() {
                return MaterialType.WOOD;
            }

            @Override
            public @NotNull List<Property> getProperties() {
                return Collections.emptyList();
            }

            @Override
            public IngredientPojo getIngredient() {
                var pojo = new IngredientPojo();
                pojo.item = "minecraft:stick";
                return pojo;
            }
        };
    }

    static SecondaryMaterial getDefaultSecondaryMaterial() {
        return new EmptySecondaryMaterial();
    }

    static ToolPartHandle getDefaultHandle() {
        return new ToolPartHandle() {
            @Override
            public String getStringIdentifier() {
                return this.getResourceName();
            }

            @Override
            public String getResourceName() {
                return "default";
            }

            @Override
            public ForgeroResourceType getResourceType() {
                return ForgeroResourceType.TOOL_PART;
            }


            @Override
            public PrimaryMaterial getPrimaryMaterial() {
                return getDefaultPrimaryMaterial();
            }

            @Override
            public SecondaryMaterial getSecondaryMaterial() {
                return getDefaultSecondaryMaterial();
            }

            @Override
            public Gem getGem() {
                return getDefaultGem();
            }

            @Override
            public AbstractToolPartState getState() {
                return new HandleState(getDefaultPrimaryMaterial(), getDefaultSecondaryMaterial(), null, null);
            }

            @Override
            public String getToolPartName() {
                return "default-handle";
            }

            @Override
            public String getToolPartIdentifier() {
                return "default-handle";
            }

            @Override
            public ForgeroToolPartTypes getToolPartType() {
                return ForgeroToolPartTypes.HANDLE;
            }

            @Override
            public Schematic getSchematic() {
                return null;
            }
        };
    }

    static Gem getDefaultGem() {
        return EmptyGem.createEmptyGem();
    }

}
