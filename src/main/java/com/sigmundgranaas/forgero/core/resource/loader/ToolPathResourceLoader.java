package com.sigmundgranaas.forgero.core.resource.loader;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPojo;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceFactory;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.resource.ResourcePathProvider;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactoryImpl;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class ToolPathResourceLoader extends PathResourceLoader<ForgeroTool, ToolPojo> {
    private final List<ForgeroToolPart> toolParts;


    public ToolPathResourceLoader(ResourcePathProvider pathProvider, Consumer<ToolPojo> handler, Function<List<ToolPojo>, ForgeroResourceFactory<ForgeroTool, ToolPojo>> factory, PojoFileLoaderImpl<ToolPojo> fileLoader, ForgeroResourceType type, List<ForgeroToolPart> toolParts) {
        super(pathProvider, handler, factory, fileLoader, type);
        this.toolParts = toolParts;
    }

    @Override
    public @NotNull ImmutableList<ForgeroTool> loadResources() {
        var toolsFromFile = super.loadResources();

        var toolPartsFromFactory = new ForgeroToolFactoryImpl().createForgeroTools(toolParts);

        return Stream.of(toolsFromFile, toolPartsFromFactory).flatMap(List::stream).collect(ImmutableList.toImmutableList());
    }
}
