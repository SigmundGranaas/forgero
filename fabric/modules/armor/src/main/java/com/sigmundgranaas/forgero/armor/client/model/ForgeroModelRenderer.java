package com.sigmundgranaas.forgero.armor.client.model;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;
import com.sigmundgranaas.forgero.model.resolution.api.ModelResolver;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A specialized renderer that bakes a Forgero Component directly into a BasicBakedModel.
 * This class manually assembles the arguments for the BasicBakedModel constructor,
 * and replicates the vanilla ItemModelGenerator's logic for creating depth and edges,
 * bypassing the need for intermediate JSON structures for maximum efficiency and control.
 */
public class ForgeroModelRenderer {
	private static final BakedQuadFactory QUAD_FACTORY = new BakedQuadFactory();
	private final Function<SpriteIdentifier, Sprite> textureGetter;
	private final ModelBakeSettings settings;
	private final ModelResolver resolver;
	private final Identifier modelId;
	private final ModelTransformation generatedTransform;
	private final ModelTransformation handheldTransform;

	public ForgeroModelRenderer(Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, ModelResolver resolver, Identifier modelId, ModelTransformation generatedTransform, ModelTransformation handheldTransform) {
		this.textureGetter = textureGetter;
		this.settings = settings;
		this.resolver = resolver;
		this.modelId = modelId;
		this.generatedTransform = generatedTransform;
		this.handheldTransform = handheldTransform;
	}

	@Nullable
	public BakedModel bake(Component component) {
		List<RenderableTexture> textures = resolver.resolve(component).orElse(Collections.emptyList());
		if (textures.isEmpty()) {
			return null;
		}

		// Prepare collections for the BasicBakedModel constructor
		List<BakedQuad> quads = new ArrayList<>();
		// The faceQuads map is required, but it will remain empty for "item/generated" type models,
		// as all their quads are in the general list.
		Map<Direction, List<BakedQuad>> faceQuads = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			faceQuads.put(dir, new ArrayList<>());
		}
		Sprite particleSprite = null;

		for (RenderableTexture texture : textures) {
			Sprite sprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(texture.texture())));
			if (particleSprite == null) {
				particleSprite = sprite;
			}

			// Create all model elements for this layer, including the main plane and side/edge planes.
			for (ModelElement element : createElementsForLayer(texture, sprite)) {
				for (Map.Entry<Direction, ModelElementFace> entry : element.faces.entrySet()) {
					Direction bakeDirection = entry.getKey();
					ModelElementFace face = entry.getValue();

					BakedQuad quad = QUAD_FACTORY.bake(element.from, element.to, face, sprite, bakeDirection, settings, element.rotation, element.shade, modelId);

					// For "item/generated" models, cullFace is always null, and all quads go into the general list.
					// This correctly mimics vanilla behavior where getQuads(null) returns everything.
					quads.add(quad);
				}
			}
		}

		if (particleSprite == null) {
			particleSprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MissingSprite.getMissingSpriteId()));
		}

		boolean usesAo = true;
		boolean sideLit = isSideLit(component); // true for flat lighting in GUI.
		boolean hasDepth = true; // The model has depth.
		// Select transform; generated items should use the "generated" transform.
		ModelTransformation transform = sideLit ? generatedTransform : handheldTransform;
		ModelOverrideList overrides = ModelOverrideList.EMPTY;

		// Construct the final BakedModel directly
		return new BasicBakedModel(quads, faceQuads, usesAo, sideLit, hasDepth, particleSprite, transform, overrides);
	}

	/**
	 * Creates all ModelElements for a single texture layer, replicating vanilla's ItemModelGenerator.
	 * This includes the main front and back faces, plus all the small side/edge faces that create depth.
	 */
	private List<ModelElement> createElementsForLayer(RenderableTexture texture, Sprite sprite) {
		List<ModelElement> elements = new ArrayList<>();
		// Use a small z-offset to prevent z-fighting between layers.
		float zOffset = 0.001f * texture.order();
		Vector2f customOffset = texture.offset() != null ? new Vector2f(texture.offset().x(), texture.offset().y()) : new Vector2f(0, 0);

		// 1. Add the main front and back plane for the texture.
		Map<Direction, ModelElementFace> mainFaces = new EnumMap<>(Direction.class);
		mainFaces.put(Direction.SOUTH, new ModelElementFace(null, -1, "", new ModelElementTexture(new float[]{0, 0, 16, 16}, 0)));
		mainFaces.put(Direction.NORTH, new ModelElementFace(null, -1, "", new ModelElementTexture(new float[]{16, 0, 0, 16}, 0)));
		elements.add(new ModelElement(
				new Vector3f(0 + customOffset.x(), 0 - customOffset.y(), 7.5f - zOffset),
				new Vector3f(16 + customOffset.x(), 16 - customOffset.y(), 8.5f - zOffset),
				mainFaces, null, true));

		// 2. Analyze the sprite's transparent pixels to generate side/edge elements.
		elements.addAll(createSideElements(sprite, texture.order(), zOffset, customOffset));

		return elements;
	}

	/**
	 * Replicates the core logic of ItemModelGenerator's addSubComponents and getFrames methods.
	 * It scans the texture for edges (a non-transparent pixel next to a transparent one)
	 * and creates thin ModelElements to form the item's "sides".
	 */
	private List<ModelElement> createSideElements(Sprite sprite, int layer, float zOffset, Vector2f customOffset) {
		List<ModelElement> elements = new ArrayList<>();
		SpriteContents contents = sprite.getContents();
		final float spriteWidth = contents.getWidth();
		final float spriteHeight = contents.getHeight();

		// Step 1: Scan the image and identify all edge segments ("frames").
		List<Frame> frames = new ArrayList<>();
		// Forgero models are not animated, so we only need to check the first frame (index 0).
		for (int y = 0; y < spriteHeight; ++y) {
			for (int x = 0; x < spriteWidth; ++x) {
				boolean isOpaque = !isPixelTransparent(contents, 0, x, y);
				if (isOpaque) {
					// For each opaque pixel, check its four neighbors. If a neighbor is transparent, we have an edge.
					checkAndBuildFrame(Side.UP, frames, contents, x, y);
					checkAndBuildFrame(Side.DOWN, frames, contents, x, y);
					checkAndBuildFrame(Side.LEFT, frames, contents, x, y);
					checkAndBuildFrame(Side.RIGHT, frames, contents, x, y);
				}
			}
		}

		// Step 2: Convert the identified frames into ModelElements.
		for (Frame frame : frames) {
			elements.add(createModelElementFromFrame(frame, layer, zOffset, customOffset, spriteWidth, spriteHeight));
		}
		return elements;
	}

	/**
	 * Checks for a transparency transition in a given direction and adds or extends a frame.
	 */
	private void checkAndBuildFrame(Side side, List<Frame> frames, SpriteContents contents, int x, int y) {
		// An edge exists if the current pixel is opaque, and the neighbor in the given direction is transparent.
		// The main loop already ensures the current pixel is opaque.
		if (isPixelTransparent(contents, 0, x + side.getOffsetX(), y + side.getOffsetY())) {
			extendOrAddFrame(frames, side, x, y);
		}
	}

	/**
	 * Finds an existing, ADJACENT frame to extend, or creates a new one.
	 * This is the corrected logic to prevent merging non-contiguous pixels.
	 */
	private void extendOrAddFrame(List<Frame> frames, Side side, int x, int y) {
		int pos = side.isVertical() ? x : y;
		int level = side.isVertical() ? y : x;

		// Try to find an adjacent frame on the same level to extend
		for (Frame currentFrame : frames) {
			if (currentFrame.getSide() == side && currentFrame.getLevel() == level && currentFrame.canExpand(pos)) {
				currentFrame.expand(pos);
				return; // Found and extended, job done.
			}
		}

		// No adjacent frame found, so create a new one.
		frames.add(new Frame(side, pos, level));
	}


	private boolean isPixelTransparent(SpriteContents contents, int frame, int x, int y) {
		int width = contents.getWidth();
		int height = contents.getHeight();
		// Pixels outside the texture bounds are considered transparent.
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return true;
		}
		return contents.isPixelTransparent(frame, x, y);
	}

	/**
	 * Creates a single, thin ModelElement from a Frame, applying all necessary transformations and UV calculations.
	 * This logic is a direct port of the coordinate math in ItemModelGenerator.addSubComponents.
	 */
	private ModelElement createModelElementFromFrame(Frame frame, int layer, float zOffset, Vector2f customOffset, float spriteWidth, float spriteHeight) {
		// These variables correspond to the cryptic ones in ItemModelGenerator
		float fromX, fromY, toX, toY;
		float u1, v1, u2, v2;

		// Scale factors to convert from sprite pixel coordinates to 16x16 model coordinates
		final float modelScaleX = 16.0f / spriteWidth;
		final float modelScaleY = 16.0f / spriteHeight;

		Side side = frame.getSide();
		float min = frame.getMin();
		float max = frame.getMax();
		float level = frame.getLevel();

		// Calculate model coordinates (from/to) and texture coordinates (uv) based on the side
		switch (side) {
			case UP:
				fromX = min;
				toX = max + 1.0f;
				fromY = level;
				toY = level;
				u1 = min;
				u2 = max + 1.0f;
				v1 = level;
				v2 = level + 1.0f;
				break;
			case DOWN:
				fromX = min;
				toX = max + 1.0f;
				fromY = level + 1.0f;
				toY = level + 1.0f;
				u1 = min;
				u2 = max + 1.0f;
				v1 = level;
				v2 = level + 1.0f;
				break;
			case LEFT:
				fromX = level;
				toX = level;
				fromY = min;
				toY = max + 1.0f;
				u1 = level;
				u2 = level + 1.0f;
				v1 = min;
				v2 = max + 1.0f;
				break;
			case RIGHT:
			default:
				fromX = level + 1.0f;
				toX = level + 1.0f;
				fromY = min;
				toY = max + 1.0f;
				u1 = level;
				u2 = level + 1.0f;
				v1 = min;
				v2 = max + 1.0f;
				break;
		}

		// Scale all coordinates to model space (0-16)
		fromX *= modelScaleX;
		toX *= modelScaleX;
		fromY *= modelScaleY;
		toY *= modelScaleY;
		u1 *= modelScaleX;
		u2 *= modelScaleX;
		v1 *= modelScaleY;
		v2 *= modelScaleY;

		// Flip Y-axis for model space (where +Y is up)
		fromY = 16.0f - fromY;
		toY = 16.0f - toY;

		// Create the face for the element
		Map<Direction, ModelElementFace> faces = new EnumMap<>(Direction.class);
		ModelElementTexture texture = new ModelElementTexture(new float[]{u1, v1, u2, v2}, 0);
		faces.put(side.getDirection(), new ModelElementFace(null, layer, "", texture));

		// Invert from/to Y if needed to ensure from < to
		if (fromY > toY) {
			float temp = fromY;
			fromY = toY;
			toY = temp;
		}

		// Create the final vectors, applying layer and custom offsets
		Vector3f from = new Vector3f(fromX + customOffset.x(), fromY - customOffset.y(), 7.5f - zOffset);
		Vector3f to = new Vector3f(toX + customOffset.x(), toY - customOffset.y(), 8.5f - zOffset);

		return new ModelElement(from, to, faces, null, true);
	}

	private boolean isSideLit(Component component) {
		return false;
	}

	/**
	 * CORRECTED Side enum.
	 * The model faces SOUTH (-Z).
	 * Therefore, the left side (-X) is WEST.
	 * The right side (+X) is EAST.
	 */
	private enum Side {
		UP(Direction.UP, 0, -1),
		DOWN(Direction.DOWN, 0, 1),
		LEFT(Direction.WEST, -1, 0), // Corrected from EAST
		RIGHT(Direction.EAST, 1, 0); // Corrected from WEST

		private final Direction direction;
		private final int offsetX;
		private final int offsetY;

		Side(Direction direction, int offsetX, int offsetY) {
			this.direction = direction;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}

		public Direction getDirection() {
			return this.direction;
		}

		public int getOffsetX() {
			return this.offsetX;
		}

		public int getOffsetY() {
			return this.offsetY;
		}

		public boolean isVertical() {
			return this == DOWN || this == UP;
		}
	}

	/**
	 * CORRECTED Frame class.
	 * Now includes logic to check for adjacency before merging.
	 */
	private static class Frame {
		private final Side side;
		private int min;
		private int max;
		private final int level;

		public Frame(Side side, int value, int level) {
			this.side = side;
			this.min = value;
			this.max = value;
			this.level = level;
		}

		public void expand(int newValue) {
			if (newValue < this.min) {
				this.min = newValue;
			} else if (newValue > this.max) {
				this.max = newValue;
			}
		}

		/**
		 * Checks if a new pixel is adjacent to the current frame segment.
		 *
		 * @param value the coordinate of the new pixel on the axis of the frame (x for vertical, y for horizontal)
		 * @return true if the new pixel is directly next to the min or max end of this frame.
		 */
		public boolean canExpand(int value) {
			return value == this.max + 1 || value == this.min - 1;
		}


		public Side getSide() {
			return this.side;
		}

		public int getMin() {
			return this.min;
		}

		public int getMax() {
			return this.max;
		}

		public int getLevel() {
			return this.level;
		}
	}
}
