package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.identifier.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.material.MaterialCollection;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForgeroToolPartFactoryImpl implements ForgeroToolPartFactory {
    private static ForgeroToolPartFactory INSTANCE;

    public static ForgeroToolPartFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroToolPartFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public @NotNull ForgeroToolPart createToolPart(@NotNull ForgeroToolPartIdentifier identifier) {
        PrimaryMaterial material = (PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(identifier.getMaterial());
        return switch (identifier.getToolPartType()) {
            case PICKAXEHEAD -> new PickaxeHead(material);
            case SWORDHEAD -> null;
            case SHOVELHEAD -> null;
            case AXEHEAD -> null;
            case HANDLE -> new Handle(material);
            case BINDING -> new Binding(material);
        };
    }

    @Override
    public @NotNull List<ForgeroToolPart> createBaseToolParts(@NotNull MaterialCollection collection) {
        return collection.getPrimaryMaterialsAsList().stream().map(this::createBaseToolPartsFromMaterial).flatMap(List::stream).collect(Collectors.toList());
    }

    private List<ForgeroToolPart> createBaseToolPartsFromMaterial(PrimaryMaterial material) {
        List<ForgeroToolPart> toolparts = new ArrayList<>();
        toolparts.add(new Handle(material));
        toolparts.add(new PickaxeHead(material));
        toolparts.add(new Binding(material));
        return toolparts;
    }
}