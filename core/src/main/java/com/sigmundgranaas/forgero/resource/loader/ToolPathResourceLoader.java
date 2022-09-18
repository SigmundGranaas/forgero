package com.sigmundgranaas.forgero.resource.loader;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.ToolPojo;
import com.sigmundgranaas.forgero.resource.FactoryProvider;
import com.sigmundgranaas.forgero.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.resource.PojoLoader;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.factory.ForgeroToolFactoryImpl;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ToolPathResourceLoader extends ResourceLoaderImpl<ForgeroTool, ToolPojo> {
    private final List<ForgeroToolPart> toolParts;


    public ToolPathResourceLoader(PojoLoader<ToolPojo> loader, Consumer<ToolPojo> handler, FactoryProvider<ForgeroTool, ToolPojo> factory, ForgeroResourceType type, List<ForgeroToolPart> toolParts) {
        super(loader, handler, factory, type);
        this.toolParts = toolParts;
    }

    @Override
    public @NotNull ImmutableList<ForgeroTool> loadResources() {
        var toolsFromFile = super.loadResources();

        var toolPartsFromFactory = new ForgeroToolFactoryImpl().createForgeroTools(toolParts);

        return Stream.of(toolsFromFile, toolPartsFromFactory).flatMap(List::stream).collect(ImmutableList.toImmutableList());
    }
}
