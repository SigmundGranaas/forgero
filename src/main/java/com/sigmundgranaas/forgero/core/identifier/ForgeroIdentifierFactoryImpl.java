package com.sigmundgranaas.forgero.core.identifier;

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
    public ForgeroMaterialIdentifier createForgeroMaterialIdentifier(String identifier) {
        String[] elements = identifier.split("_");
        return new ForgeroMaterialIdentifierImpl(elements[0]);
    }

    private ForgeroIdentifier createForgeroIdentifierFromName(String forgeroName) {
        String[] elements = forgeroName.split("_");
        return switch (elements.length) {
            case 6 -> new ForgeroToolIdentifierImpl(forgeroName);
            case 2 -> createForgeroToolIdentifier(forgeroName);
            case 1 -> new ForgeroMaterialIdentifierImpl(forgeroName);
            default -> throw new IllegalStateException("Unexpected value: " + elements.length);
        };

    }

    private ForgeroIdentifier createForgeroToolIdentifier(String forgeroName) {
        if (forgeroName.contains("head")) {
            return new ForgeroToolPartHeadIdentifier(forgeroName);
        } else {
            return new ForgeroToolPartIdentifierImpl(forgeroName);
        }
    }
}
