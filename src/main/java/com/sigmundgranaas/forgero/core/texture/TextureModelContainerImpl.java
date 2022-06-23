package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ModelContainerPojo;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ModelPojo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TextureModelContainerImpl implements TextureModelContainer {
    private final Map<String, TextureModel> models;

    private final TextureModel defaultModel;

    public TextureModelContainerImpl(Map<String, TextureModel> models, TextureModel defaultModel) {
        this.models = models;
        this.defaultModel = defaultModel;
    }

    public static Optional<ModelContainerPojo> resolvePojoReference(Collection<ModelContainerPojo> pojos, @NotNull ModelContainerPojo currentPojo) {
        if (currentPojo.model == null) {
            var parent = pojos.stream().filter(filteredPojo -> filteredPojo.id.equals(currentPojo.parent)).findAny();
            if (parent.isEmpty()) {
                var defaultModel = pojos.stream().filter(filteredPojo -> filteredPojo.id.equals("DEFAULT")).findAny();
                if (defaultModel.isEmpty()) {
                    ForgeroInitializer.LOGGER.warn("Cannot read model from pojo, as it does not exist");
                    return Optional.empty();
                } else {
                    return defaultModel;
                }
            } else {
                return resolvePojoReference(pojos, parent.get());
            }
        }
        return Optional.of(currentPojo);
    }

    public static Optional<TextureModelContainer> createContainer(Collection<ModelContainerPojo> pojos) {
        try {
            var resolvedModels = pojos.stream().map(container -> resolvePojoReference(pojos, container)).flatMap(Optional::stream).toList();
            var defaultModel = resolvedModels.stream().filter(modelContainerPojo -> modelContainerPojo.id.equals("DEFAULT")).findAny();
            return defaultModel.map(model -> new TextureModelContainerImpl(resolvedModels.stream().collect(Collectors.toMap((ModelContainerPojo container) -> container.id, (ModelContainerPojo container) -> createTextureModel(container.model))), createTextureModel(defaultModel.get().model)));
        } catch (Exception e) {
            ForgeroInitializer.LOGGER.warn(e);
        }
        return Optional.empty();
    }


    public static TextureModel createTextureModel(ModelPojo pojo) {
        return new TextureModel(pojo.primary, pojo.secondary, pojo.gem);
    }

    @Override
    public @NotNull TextureModel getModel(String id) {
        return models.getOrDefault(id, defaultModel);
    }

    @Override
    public @NotNull TextureModel getModel(ToolPartModelType id) {
        return models.getOrDefault(id.name(), defaultModel);
    }

    @Override
    public @NotNull TextureModel getModel() {
        return defaultModel;
    }

    @Override
    public @NotNull Collection<TextureModel> getModels() {
        return models.values();
    }
}
