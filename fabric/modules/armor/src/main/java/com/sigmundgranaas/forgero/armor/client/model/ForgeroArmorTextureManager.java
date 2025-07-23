package com.sigmundgranaas.forgero.armor.client.model;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.model.api.*;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.api.item.CompositeModel;
import com.sigmundgranaas.forgero.model.api.item.EmptyModel;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.api.item.TextureModel;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.rendering.api.TextureCompositor;
import com.sigmundgranaas.forgero.model.rendering.impl.AwtTextureCompositor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ForgeroArmorTextureManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroArmorTextureManager.class);
	private final TextureCompositor compositor;
	private final ItemModelRegistry itemModelRegistry;
	private final Map<Integer, Identifier> textureCache = new ConcurrentHashMap<>();

	public ForgeroArmorTextureManager(ItemModelRegistry itemModelRegistry) {
		this.itemModelRegistry = itemModelRegistry;
		this.compositor = new AwtTextureCompositor(new com.sigmundgranaas.forgero.model.rendering.impl.ClassPathResourceTextureProvider());
	}

	public Identifier getTexture(ArmorModel armorModel, Component component) {
		int cacheKey = Objects.hash(armorModel, component);
		return textureCache.computeIfAbsent(cacheKey, key -> {
			List<RenderableTexture> textureLayers = resolveTextureLayers(armorModel, component);
			BufferedImage composedImage = compositor.render(textureLayers);
			Identifier dynamicTextureId = new Identifier("forgero", "dynamic/armor/" + component.id().path() + "_" + cacheKey);

			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				ImageIO.write(composedImage, "PNG", os);
				byte[] imageBytes = os.toByteArray();

				// Ensure texture registration happens on the render thread.
				MinecraftClient.getInstance().execute(() -> {
					try (ByteArrayInputStream is = new ByteArrayInputStream(imageBytes)) {
						// Create a NativeImage from the byte stream of the composed AWT image.
						NativeImage nativeImage = NativeImage.read(is);
						// Create the texture object that Minecraft's TextureManager can handle.
						NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
						// Register the texture with the manager. If an old texture with the same ID exists, it will be replaced.
						MinecraftClient.getInstance().getTextureManager().registerTexture(dynamicTextureId, texture);
					} catch (IOException e) {
						LOGGER.error("Failed to create NativeImage for dynamic texture: {}", dynamicTextureId, e);
					}
				});
			} catch (IOException e) {
				LOGGER.error("Failed to write composed image to byte array for texture: {}", dynamicTextureId, e);
			}

			return dynamicTextureId;
		});
	}

	/**
	 * Resolves the final list of texture layers to be composited for a given armor model and component state.
	 */
	private List<RenderableTexture> resolveTextureLayers(ArmorModel armorModel, Component sourceComponent) {
		List<RenderableTexture> textures = new ArrayList<>();
		ModelResolutionContext context = new ModelResolutionContext(sourceComponent, sourceComponent);

		for (ModelLayer layer : armorModel.textures()) {
			textures.add(getLayerTexture(layer, context, 0));
		}

		if (sourceComponent instanceof StructuredComponent structured) {
			Map<String, Component> filledSlots = structured.structure().slots().values().stream()
					.collect(Collectors.toMap(slot -> slot.id().path(), StructureSlot::content));

			for (ModelSlot modelSlot : armorModel.slots()) {
				Component childComponent = filledSlots.get(modelSlot.id());
				if (childComponent != null) {
					textures.addAll(resolveSlotAsTexture(modelSlot, childComponent, context, modelSlot.order()));
				}
			}
		}
		return textures.stream().sorted().collect(Collectors.toList());
	}

	private List<RenderableTexture> resolveSlotAsTexture(ModelSlot modelSlot, Component child, ModelResolutionContext parentContext, int baseOrder) {
		ModelResolutionContext childContext = parentContext.with(child);

		Optional<Model> modelOpt = modelSlot.context()
				.flatMap(ctx -> itemModelRegistry.find(child.id(), ctx))
				.or(() -> itemModelRegistry.find(child.id()));

		return modelOpt.map(model -> collectTexturesFromItemModel(model, child, childContext).stream()
						.map(texture -> texture.withOrder(baseOrder + texture.order()))
						.toList())
				.orElse(Collections.emptyList());
	}

	private List<RenderableTexture> collectTexturesFromItemModel(Model model, Component component, ModelResolutionContext context) {
		Model resolvedModel = model.apply(context);
		if (resolvedModel instanceof EmptyModel) {
			return Collections.emptyList();
		}

		if (resolvedModel instanceof CompositeModel composite) {
			List<RenderableTexture> textures = new ArrayList<>();
			for (ModelLayer layer : composite.layers()) {
				textures.add(getLayerTexture(layer, context, 0));
			}

			if (component instanceof StructuredComponent structured) {
				Map<String, Component> filledSlots = structured.structure().slots().values().stream()
						.collect(Collectors.toMap(slot -> slot.id().path(), StructureSlot::content));

				for (ModelSlot modelSlot : composite.slots()) {
					Component childComponent = filledSlots.get(modelSlot.id());
					if (childComponent != null) {
						textures.addAll(resolveSlotAsTexture(modelSlot, childComponent, context, modelSlot.order()));
					}
				}
			}
			return textures;
		} else if (resolvedModel instanceof TextureModel textureModel) {
			return List.of(getLayerTexture(new ModelLayer(textureModel.texture(), 0, textureModel.variants(), textureModel.offset()), context, 0));
		}
		return Collections.emptyList();
	}

	private RenderableTexture getLayerTexture(ModelLayer layer, ModelResolutionContext context, int baseOrder) {
		return layer.getActiveVariant(context)
				.map(variant -> new RenderableTexture(
						variant.texture().orElse(layer.texture()),
						baseOrder + layer.order(),
						variant.offset().orElse(layer.offset().orElse(Offset.ZERO)))
				)
				.orElse(new RenderableTexture(
						layer.texture(),
						baseOrder + layer.order(),
						layer.offset().orElse(Offset.ZERO))
				);
	}
}
