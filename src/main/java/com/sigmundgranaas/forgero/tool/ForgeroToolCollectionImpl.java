package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;

import java.util.List;

public class ForgeroToolCollectionImpl implements ForgeroToolCollection {
    public static ForgeroToolCollection INSTANCE;

    public static ForgeroToolCollection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroToolCollectionImpl();
        }
        return INSTANCE;
    }

    @Override
    public List<ForgeroToolPart> getToolsParts() {
        return null;
    }

    @Override
    public List<ForgeroTool> getTools() {
        return null;
    }
}
