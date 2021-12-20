package com.sigmundgranaas.forgero.identifier;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.stream.Stream;

public class ForgeroIdentifierFactoryImpl implements ForgeroIdentifierFactory {
    private static ForgeroIdentifierFactoryImpl INSTANCE;

    public static ForgeroIdentifierFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroIdentifierFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public ForgeroIdentifier createForgeroIdentifier(Identifier identifier) {
        String removePath = Stream.of(identifier.getPath().split("/"))
                .reduce((first, second) -> second)
                .orElse("")
                .replace(".png", "")
                .replace("#inventory", "");
        return createForgeroIdentifier(removePath);
    }

    @Override
    public ForgeroIdentifier createForgeroIdentifier(String identifier) {
        return createForgeroIdentifierFromName(identifier);
    }

    @Override
    public ForgeroIdentifier createForgeroIdentifier(Item item) {
        return null;
    }

    private ForgeroIdentifier createForgeroIdentifierFromName(String forgeroName) {
        String[] elements = forgeroName.split("_");
        return switch (elements.length) {
            case 6 -> new ForgeroToolIdentifierImpl(forgeroName);
            case 2 -> new ForgeroToolPartIdentifierImpl(forgeroName);
            case 1 -> new ForgeroMaterialIdentifierImpl(forgeroName);
            default -> throw new IllegalStateException("Unexpected value: " + elements.length);
        };

    }

}
