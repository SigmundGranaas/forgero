package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.Constants;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Optional;

public class ToolTextureManager {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private static ToolTextureManager manager = null;
    private final HashMap<ToolMaterial, MaterialColourPalette> materialPalettes = new HashMap<>();
    private final HashMap<ForgeroToolPartTypes, BaseTexture> templateTextures = new HashMap<>();

    public static ToolTextureManager getInstance() {
        if (manager == null) {
            manager = new ToolTextureManager();
        }
        return manager;
    }

    /**
     * A method which will check if a resource is prepared, and create the texture if it does not exist.
     * This method will not do anything if it finds an existing texture, or if the operation fails.
     *
     * @param id          Identifier for the requested texture
     * @param getResource A method for accessing the potential resource dependency
     * @throws IOException If a resource is not found, an IOException will be thrown.
     */
    public void createTextureDependencies(Identifier id, BiConsumer_WithExceptions<Identifier, Resource> getResource) throws IOException {
        if (resourceExists(id)) {
            return;
        }


        Optional<BaseTexture> templateTextureResult = getOrCreateTemplateTexture(id);

        Optional<MaterialColourPalette> paletteResult = getOrCreateMaterialPalette(id, getResource);

        if (templateTextureResult.isEmpty() || paletteResult.isEmpty()) {
            return;
        }

        BufferedImage recolouredTexture = templateTextureResult.get().createRecolouredImage(paletteResult.get());

        try {
            writeTextureToFile(id, recolouredTexture);
        } catch (Exception e) {

        }

        Resource plankTexture = getResource.apply(new Identifier("minecraft:textures/item/birch_boat.png"));
        InputStream stream = plankTexture.getInputStream();
        BufferedImage texture;
        ImageIO.setUseCache(false);


        try {
            texture = ImageIO.read(stream);
        } catch (IOException e) {

        }
    }

    private void writeTextureToFile(Identifier id, BufferedImage recolouredTexture) {
    }


    private Optional<MaterialColourPalette> getOrCreateMaterialPalette(Identifier id, BiConsumer_WithExceptions<Identifier, Resource> getResource) {
        String toolPartName = getResourceNameFromID(id);
        Optional<ToolMaterial> materialResult = ForgeroToolPartItem.getMaterialFromFileName(toolPartName);
        if (materialResult.isEmpty()) {
            return Optional.empty();
        }
        ToolMaterial material = materialResult.get();
        if (materialPalettes.containsKey(material)) {
            return Optional.of(materialPalettes.get(material));
        }

        Identifier materialDependencyIdentifier = getMaterialDependencyFromMaterial(material);

        try {
            Resource materialTexture = getResource.apply(materialDependencyIdentifier);
            BufferedInputStream materialTextureStream = new BufferedInputStream(materialTexture.getInputStream());
            BufferedImage materialTextureImage = ImageIO.read(materialTextureStream);
            return Optional.of(MaterialColourPalette.createColourPalette(materialTextureImage));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Identifier getMaterialDependencyFromMaterial(ToolMaterial material) {
        if (material == ToolMaterials.WOOD) {
            return new Identifier("minecraft:textures/item/oak_boat.png");
        } else {
            return new Identifier("minecraft:textures/item/iron_armor.png");
        }
    }

    private Optional<BaseTexture> getOrCreateTemplateTexture(Identifier id) {
        String fileName = getResourceNameFromID(id);
        Optional<ForgeroToolPartTypes> toolPartResult = ForgeroToolPartItem.getToolPartTypeFromFileName(fileName);

        if (toolPartResult.isEmpty()) {
            return Optional.empty();
        }
        ForgeroToolPartTypes toolPart = toolPartResult.get();

        if (templateTextures.containsKey(toolPart)) {
            return Optional.of(templateTextures.get(toolPart));
        }

        Optional<BaseTexture> BaseTextureResult = createBaseTexture(toolPart);

        if (BaseTextureResult.isEmpty()) {
            return Optional.empty();
        }
        BaseTexture baseTexture = BaseTextureResult.get();
        templateTextures.put(toolPart, baseTexture);
        return Optional.of(baseTexture);
    }

    private Optional<BaseTexture> createBaseTexture(ForgeroToolPartTypes toolPart) {
        String textureName = getTextureNameFromToolPart(toolPart);
        String baseTextureName = textureName + Constants.BASE_IDENTIFIER;
        String baseTextureFullPath = Constants.CONFIG_PATH + "templates/toolparts/" + baseTextureName + ".png";

        File targetFile = new File(baseTextureFullPath);
        if (!targetFile.exists()) {
            return Optional.empty();
        }

        BufferedImage templateImage;
        try {
            BufferedInputStream imageStream = new BufferedInputStream(new FileInputStream(baseTextureFullPath));
            templateImage = ImageIO.read(imageStream);

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        if (templateImage == null) {
            return Optional.empty();
        }

        BaseTexture baseTexture = BaseTexture.createBaseTexture(templateImage);
        templateTextures.put(toolPart, baseTexture);
        return Optional.of(baseTexture);
    }

    private String getTextureNameFromToolPart(ForgeroToolPartTypes toolPart) {
        return switch (toolPart) {
            case PICKAXEHEAD -> Constants.PICKAXEHEAD_FILENAME;
            case SHOVELHEAD -> Constants.SHOVELHEAD_FILENAME;
            case HANDLE -> Constants.FULLHANDLE_FILENAME;
            default -> "";
        };
    }

    private String getResourceNameFromID(Identifier id) {
        String texturePath = id.getPath();
        String[] elements = texturePath.split("/");
        String fileName = elements[elements.length - 1];
        fileName = fileName.replace(".png", "");
        return fileName;
    }

    private boolean resourceExists(Identifier id) {
        File targetFile = new File("src/main/resources/assets/forgero" + getFileNameFromId(id));
        return targetFile.exists();
    }

    private String getFileNameFromId(Identifier id) {
        return id.getPath();
    }


    @FunctionalInterface
    public interface BiConsumer_WithExceptions<Identifier, Resource> {
        Resource apply(Identifier id) throws IOException;
    }
}

