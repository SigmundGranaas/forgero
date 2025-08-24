package com.sigmundgranaas.forgero.smithing.item.renderer;

import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

@Environment(EnvType.CLIENT)
public class MorphedItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	private static final Logger LOGGER = LogManager.getLogger("ForgeroMorphedItemRenderer");

	@Override
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();

		switch (mode) {
			case GUI -> {
				// Center in inventory slot
				matrices.translate(0.5, 0.5, 0);
				matrices.scale(1f, 1f, 1f);
			}
			case FIXED -> {
				// Center in inventory slot
				matrices.translate(0.5, 0.5, 0.5); // perfectly centered
				matrices.scale(0.5f, 0.5f, 0.5f);  // half size
			}
			case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
				// Vanilla hand position
				matrices.translate(0, 0.25, 0);
				matrices.scale(1f, 1f, 1f);
			}
			case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
				// Vanilla third-person in-hand
				matrices.scale(0.5f, 0.5f, 0.5f);
			}
			case GROUND -> {
				// Dropped items or item frames
				matrices.translate(0.5, 0.4, 0.5); // center + a bit up
				matrices.scale(1f, 1f, 1f);   // half size
			}
			case NONE -> {
				// Dropped items or item frames
				matrices.translate(0.5, 0.5, 0.5); // center + a bit up
				matrices.scale(1f, 1f, 1f);   // half size
			}
		}

		NbtCompound nbt = stack.getOrCreateNbt();
		double weightRaw = nbt.contains(MorphedItem.PROGRESS_KEY) ? nbt.getDouble(MorphedItem.PROGRESS_KEY) : 0.0;
		float weight = MathHelper.clamp((float) weightRaw, 0.0f, 1.0f);

		Identifier startId = MorphedItem.getStartItemId(stack);
		Identifier resultId = MorphedItem.getResultItemId(stack);

		ItemStack toRender = ItemStack.EMPTY;
		if (startId != null && resultId != null) {
			// Pick whichever item is closest to current progress
			Identifier chosen = (weight < 0.5f) ? startId : resultId;
			Item item = Registries.ITEM.get(chosen);
			if (item != null) toRender = new ItemStack(item);
		} else if (startId != null) {
			Item item = Registries.ITEM.get(startId);
			if (item != null) toRender = new ItemStack(item);
		} else if (resultId != null) {
			Item item = Registries.ITEM.get(resultId);
			if (item != null) toRender = new ItemStack(item);
		}

		if (!toRender.isEmpty()) {
			// Get the model for the item we want to render
			BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModel(toRender, null, null, 0);

			// Render the model directly without additional transformations
			MinecraftClient.getInstance().getItemRenderer().renderItem(
					toRender, mode, false, matrices, vertexConsumers, light, overlay, model);
			matrices.pop();
			return;
		}

		// Fallback: if no start/result defined, render nothing
		LOGGER.debug("MorphedItemRenderer: no start/result item defined for {}", stack.getItem());
		matrices.pop();
	}
}
