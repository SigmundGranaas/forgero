package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;

import java.util.List;

public interface ForgeroToolCollection {
    ForgeroToolCollection INSTANCE = ForgeroToolCollectionImpl.getInstance();

    List<ForgeroToolPart> getToolsParts();

    List<ForgeroTool> getTools();
}
