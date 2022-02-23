package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticPrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticSecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.strategy.BindingMaterialStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HandleMaterialStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HeadMaterialStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.realistic.RealisticBindingStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.realistic.RealisticHandleStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.realistic.RealisticHeadStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.simple.SimpleBindingStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.simple.SimpleHandleStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.simple.SimpleMaterialHeadStrategy;

public interface ToolPartStrategyFactory {

    static HeadMaterialStrategy createToolPartHeadStrategy(ForgeroToolTypes head, PrimaryMaterial primary) {
        return createToolPartHeadStrategy(head, primary, new EmptySecondaryMaterial());
    }

    static HeadMaterialStrategy createToolPartHeadStrategy(ForgeroToolTypes head, PrimaryMaterial primary, SecondaryMaterial secondary) {
        if (primary instanceof RealisticPrimaryMaterial) {
            return switch (head) {
                case PICKAXE -> new RealisticHeadStrategy((RealisticPrimaryMaterial) primary);
                case SHOVEL -> new RealisticHeadStrategy((RealisticPrimaryMaterial) primary);
                case AXE -> new RealisticHeadStrategy((RealisticPrimaryMaterial) primary);
                case SWORD -> null;
            };
        } else if (primary instanceof SimplePrimaryMaterial && secondary instanceof SimpleSecondaryMaterial) {
            return new SimpleMaterialHeadStrategy((SimplePrimaryMaterial) primary, (SimpleSecondaryMaterial) secondary);
        } else {
            throw new IllegalArgumentException("Unknown type of material");
        }
    }

    static HandleMaterialStrategy createToolPartHandleStrategy(PrimaryMaterial primary, SecondaryMaterial secondary) {
        //TODO add complete branching
        if (primary instanceof RealisticPrimaryMaterial && secondary instanceof RealisticSecondaryMaterial) {
            return new RealisticHandleStrategy((RealisticPrimaryMaterial) primary, (RealisticSecondaryMaterial) secondary);
        } else if (primary instanceof SimplePrimaryMaterial && secondary instanceof SimpleSecondaryMaterial) {
            return new SimpleHandleStrategy((SimplePrimaryMaterial) primary, (SimpleSecondaryMaterial) secondary);
        } else {
            throw new IllegalArgumentException("Unknown type of material");
        }

    }

    static HandleMaterialStrategy createToolPartHandleStrategy(PrimaryMaterial primary) {
        return createToolPartHandleStrategy(primary, new EmptySecondaryMaterial());

    }

    static BindingMaterialStrategy createToolPartBinding(PrimaryMaterial primary) {
        return createToolPartBinding(primary, new EmptySecondaryMaterial());
    }

    static BindingMaterialStrategy createToolPartBinding(PrimaryMaterial primary, SecondaryMaterial secondary) {
        if (primary instanceof RealisticPrimaryMaterial && secondary instanceof RealisticSecondaryMaterial) {
            return new RealisticBindingStrategy((RealisticPrimaryMaterial) primary, (RealisticSecondaryMaterial) secondary);
        } else if (primary instanceof SimplePrimaryMaterial && secondary instanceof SimpleSecondaryMaterial) {
            return new SimpleBindingStrategy((SimplePrimaryMaterial) primary, (SimpleSecondaryMaterial) secondary);
        } else {
            throw new IllegalArgumentException(String.format("Unknown type of materials.json, primary: %s, secondary: %s", primary.getClass().getName(), secondary.getClass().getName()));
        }
    }
}

