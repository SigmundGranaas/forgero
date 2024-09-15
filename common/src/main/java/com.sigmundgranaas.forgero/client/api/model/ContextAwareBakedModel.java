package com.sigmundgranaas.forgero.client.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.client.impl.model.RenderContextManager;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

/**
 * Interface {@code ContextAwareBakedModel} extends {@code BakedModel} to offer enhanced functionality for models
 * that need additional context during rendering. It is particularly useful for models that must adapt their appearance
 * based on the item being rendered, the world context, the entity involved, and a given seed for randomization.
 */
public interface ContextAwareBakedModel extends BakedModel {
	/**
	 * Retrieves the current rendering context, if available.
	 *
	 * @return An {@code Optional} of {@code RenderContext}, which is present if the context is set; otherwise, an empty {@code Optional}.
	 */
	static Optional<RenderContext> getCurrentContext() {
		return RenderContextManager.getCurrentContext();
	}

	/**
	 * Returns a list of {@code BakedQuad} based on the context provided. This method allows the model to be rendered
	 * appropriately by using the current context
	 *
	 * @param stack  The {@code ItemStack} being rendered.
	 * @param world  The {@code ClientWorld} in which the rendering occurs.
	 * @param entity The {@code LivingEntity} holding the item. Can be null if not applicable.
	 * @param seed   A seed for randomization, used to generate different render outcomes.
	 * @param face   The face of the block being queried, or null if not specific to any block face.
	 * @param random A {@code Random} instance for random number generation.
	 * @return A list of {@code BakedQuad} for the model, tailored to the provided context.
	 */
	List<BakedQuad> getQuadsWithContext(@Nullable ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed, @Nullable Direction face, Random random);

	default List<BakedQuad> defaultQuads(@Nullable Direction face, Random random) {
		return Collections.emptyList();
	}

	@Override
	default Sprite getParticleSprite() {
		Optional<RenderContext> ctx = getCurrentContext();
		if (ctx.isPresent()) {
			RenderContext context = ctx.get();
			return getParticleSpriteWithContext(context.stack(), context.world(), context.entity(), context.seed());
		} else {
			return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("block/cobblestone"));
		}
	}

	@Override
	default ModelTransformation getTransformation() {
		Optional<RenderContext> ctx = getCurrentContext();
		if (ctx.isPresent()) {
			RenderContext context = ctx.get();
			return getTransformationWithContext(context.stack(), context.world(), context.entity(), context.seed());
		} else {
			return ModelTransformation.NONE;
		}
	}

	@Override
	default ModelOverrideList getOverrides() {
		Optional<RenderContext> ctx = getCurrentContext();
		if (ctx.isPresent()) {
			RenderContext context = ctx.get();
			return getOverridesWithContext(context.stack(), context.world(), context.entity(), context.seed());
		} else {
			return ModelOverrideList.EMPTY;
		}
	}

	Sprite getParticleSpriteWithContext(ItemStack stack, ClientWorld world, LivingEntity entity, int seed);

	ModelTransformation getTransformationWithContext(ItemStack stack, ClientWorld world, LivingEntity entity, int seed);

	ModelOverrideList getOverridesWithContext(ItemStack stack, ClientWorld world, LivingEntity entity, int seed);

	/**
	 * Default method override to integrate context awareness into model rendering. It checks for context availability
	 * and calls {@code getQuadsWithContext} if context is present, otherwise falls back to {@code defaultQuads}.
	 *
	 * @param state  The block state for which quads are requested, can be null.
	 * @param face   The block face for which quads are requested, can be null.
	 * @param random The {@code Random} instance to use for random number generation.
	 * @return A list of {@code BakedQuad} appropriate for the given parameters and context.
	 */
	@Override
	default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		Optional<RenderContext> ctx = getCurrentContext();
		if (ctx.isPresent()) {
			RenderContext context = ctx.get();
			return getQuadsWithContext(context.stack(), context.world(), context.entity(), context.seed(), face, random);
		} else {
			return defaultQuads(face, random);
		}
	}

	/**
	 * Record to encapsulate the rendering context which includes an item stack, a world, an entity, and a seed.
	 *
	 * @param stack  The {@code ItemStack} involved in the rendering.
	 * @param world  The {@code ClientWorld} where the rendering takes place.
	 * @param entity The {@code LivingEntity} holding or interacting with the item.
	 * @param seed   An integer seed for use in randomization during rendering.
	 */
	record RenderContext(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
	}
}
