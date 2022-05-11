package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.MaterialType;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartState;
import com.sigmundgranaas.forgero.core.toolpart.handle.HandleState;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface ForgeroDefaults {
    static PrimaryMaterial getDefaultPrimaryMaterial() {
        return new PrimaryMaterial() {
            @Override
            public int getRarity() {
                return 0;
            }

            @Override
            public @NotNull String getName() {
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
            public String getIngredient() {
                return "minecraft:stick";
            }
        };
    }

    static SecondaryMaterial getDefaultSecondaryMaterial() {
        return new EmptySecondaryMaterial();
    }

    static ToolPartHandle getDefaultHandle() {
        return new ToolPartHandle() {
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
            public ToolPartState getState() {
                return new HandleState(getDefaultPrimaryMaterial(), getDefaultSecondaryMaterial(), null, null);
            }

            @Override
            public String getToolPartName() {
                return "default_handle";
            }

            @Override
            public String getToolPartIdentifier() {
                return "default_handle";
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
