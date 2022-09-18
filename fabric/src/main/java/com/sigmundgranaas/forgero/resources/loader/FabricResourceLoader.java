package com.sigmundgranaas.forgero.resources.loader;

import com.sigmundgranaas.forgero.ForgeroResourceLoader;
import com.sigmundgranaas.forgero.ResourceLoader;
import com.sigmundgranaas.forgero.resource.data.factory.*;
import com.sigmundgranaas.forgero.resource.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgero.resource.FactoryProvider;
import com.sigmundgranaas.forgero.resource.ForgeroResource;
import com.sigmundgranaas.forgero.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.resource.PojoLoader;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.*;
import com.sigmundgranaas.forgero.resource.loader.PojoFileLoaderImpl;
import com.sigmundgranaas.forgero.resource.loader.ResourceLoaderImpl;
import com.sigmundgranaas.forgero.resource.loader.ToolPartPathResourceLoader;
import com.sigmundgranaas.forgero.resource.loader.ToolPathResourceLoader;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class FabricResourceLoader implements ForgeroResourceLoader {
    private final Set<String> availableNameSpaces;

    protected List<PrimaryMaterial> materials;
    protected List<Schematic> schematics;
    protected List<ForgeroToolPart> toolParts;

    public FabricResourceLoader(Set<String> availableNameSpaces) {
        this.materials = new ArrayList<>();
        this.schematics = new ArrayList<>();
        this.toolParts = new ArrayList<>();
        this.availableNameSpaces = availableNameSpaces;
    }

    @Override
    public @NotNull ResourceLoader<Schematic, SchematicPojo> getSchematicLoader() {
        FactoryProvider<Schematic, SchematicPojo> provider = (pojos) -> new SchematicFactory(pojos, availableNameSpaces);

        var loader = new ResourceLoaderImpl<>(
                createPojoLoader(SchematicPojo.class, ForgeroResourceType.SCHEMATIC),
                provider,
                ForgeroResourceType.SCHEMATIC);

        schematics = loader.loadResources();

        return loader;
    }

    @Override
    public @NotNull ResourceLoader<ForgeroMaterial, MaterialPojo> getMaterialLoader() {
        FactoryProvider<ForgeroMaterial, MaterialPojo> provider = (pojos) -> new MaterialFactoryImpl(pojos, getAvailableNameSpaces());

        var loader = new ResourceLoaderImpl<>(
                createPojoLoader(MaterialPojo.class, ForgeroResourceType.MATERIAL),
                (materialPojo) -> registerPalettes(materialPojo.palette),
                provider,
                ForgeroResourceType.MATERIAL);

        this.materials = loader.loadResources().stream().filter(PrimaryMaterial.class::isInstance).map(PrimaryMaterial.class::cast).toList();

        return loader;
    }

    @Override
    public @NotNull ResourceLoader<Gem, GemPojo> getGemLoader() {
        FactoryProvider<Gem, GemPojo> provider = (pojos) -> new GemFactory(pojos, getAvailableNameSpaces());

        return new ResourceLoaderImpl<>(
                createPojoLoader(GemPojo.class, ForgeroResourceType.GEM),
                (gemPojo) -> registerPalettes(gemPojo.palette),
                provider,
                ForgeroResourceType.GEM);
    }

    @Override
    public @NotNull ResourceLoader<ForgeroToolPart, ToolPartPojo> getToolPartLoader() {
        var loader = new ToolPartPathResourceLoader(
                createPojoLoader(ToolPartPojo.class, ForgeroResourceType.TOOL_PART),
                (pojo) -> {
                },
                (pojo) -> new ToolPartFactory(pojo, availableNameSpaces),
                ForgeroResourceType.TOOL_PART,
                materials,
                schematics);
        toolParts = loader.loadResources();
        return loader;
    }

    @Override
    public @NotNull ResourceLoader<ForgeroTool, ToolPojo> getToolLoader() {
        return new ToolPathResourceLoader(
                createPojoLoader(ToolPojo.class, ForgeroResourceType.TOOL),
                (pojo) -> {
                },
                (pojo) -> new ToolFactory(pojo, availableNameSpaces),
                ForgeroResourceType.TOOL,
                toolParts);
    }

    @Override
    public Set<String> getAvailableNameSpaces() {
        return availableNameSpaces;
    }

    @SuppressWarnings("SameParameterValue")
    protected <T extends ForgeroResource<R>, R extends ForgeroDataResource> ResourceLoader<T, R> createResourceLoader(Class<R> classType, ForgeroResourceType type, FactoryProvider<T, R> factoryProvider) {
        var fileLoader = createPojoLoader(classType, type);
        return new ResourceLoaderImpl<>(
                fileLoader,
                factoryProvider,
                type
        );
    }

    protected <T extends ForgeroResource<R>, R extends ForgeroDataResource> ResourceLoader<T, R> createResourceLoader(Class<R> classType, Consumer<R> handler, ForgeroResourceType type, FactoryProvider<T, R> factoryProvider) {
        return new ResourceLoaderImpl<T, R>(
                createPojoLoader(classType, type),
                handler,
                factoryProvider,
                type
        );
    }


    protected void registerPalettes(PalettePojo pojo) {
        if (pojo != null && pojo.include != null) {
            List<ResourceIdentifier> inclusions = pojo.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.name), paletteIdentifiers)).toList();
            List<ResourceIdentifier> exclusions = new ArrayList<>();
            if (pojo.exclude != null) {
                exclusions = pojo.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.name), paletteIdentifiers)).toList();
            }
            PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.name, inclusions, exclusions));
        }
    }

    protected <R extends ForgeroDataResource> PojoLoader<R> createPojoLoader(Class<R> classType, ForgeroResourceType type) {
        return new PojoFileLoaderImpl<>(FabricPathProvider.PROVIDER.getPaths(type), classType);
    }
}
