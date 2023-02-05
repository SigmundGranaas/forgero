package com.sigmundgranaas.forgero.fabric.modmenu;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.fabric.modmenu.gui.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.List;

public class ForgeroConfigurationScreen extends GameOptionsScreen {
    public static ForgeroConfigurationScreen INSTANCE;

    private final Screen previous;
    private ButtonListWidget list;
    
    public ForgeroConfigurationScreen(Screen previous) {
        super(previous, MinecraftClient.getInstance().options, Text.translatable("forgero.menu.options"));
        this.previous = previous;

        INSTANCE = this;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return true;
    }

    @Override
    protected void init() {
        super.init();

        BuildConfigScreen();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Render background texture with vOffset -100 to match Mod Menu's vOffset
        this.renderBackgroundTexture(-100);

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);

        for (Element element : children()) {
            if (element instanceof Drawable drawable) {
                drawable.render(matrices, mouseX, mouseY, delta);
            }
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    public void BuildConfigScreen() {
        try {
            int y = this.height / 6;
            int booleanWidgetWithResetHeight = 20;
            int padding = 5;

            for (Field field : ForgeroConfigurationLoader.configuration.getClass().getFields()) {
                var value = field.get(ForgeroConfigurationLoader.configuration);

                if (value instanceof Boolean) {
                    createBooleanWidgetWithReset(
                            this.width / 2 - 200,
                            y,
                            Text.translatable(MessageFormat.format("forgero.menu.options.{0}", field.getName())),
                            ForgeroConfigurationLoader.configuration,
                            field
                    );
                } else if (value instanceof List) {
                    createListWidgetWithReset(
                            this.width / 2 - 200,
                            y,
                            Text.translatable(MessageFormat.format("forgero.menu.options.{0}", field.getName())),
                            ForgeroConfigurationLoader.configuration,
                            field
                    );
                } else if (value instanceof Integer) {
                    createIntegerWidgetWithReset(this.width / 2 - 200,
                            y,
                            Text.translatable(MessageFormat.format("forgero.menu.options.{0}", field.getName())),
                            ForgeroConfigurationLoader.configuration,
                            field);
                }

                y += booleanWidgetWithResetHeight + padding;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 154, this.height - 28, 150, 20, Text.translatable("forgero.menu.options.reload_config"), button -> {
            ForgeroConfigurationLoader.load();
            RebuildConfigScreen();
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 4, this.height - 28, 150, 20, ScreenTexts.DONE, button -> {
            close();
        }));
    }

    // FIXME: This is a hack that shouldn't be necessary if the widget creation functions are moved into their own class which can track the states
    public void RebuildConfigScreen() {
        clearChildren();
        BuildConfigScreen();
    }

    // FIXME: Refactor this into its own class
    public void createBooleanWidgetWithReset(int x, int y, Text optionName, Object object, Field field) {
        this.addDrawableChild(new TextWidget(x, y - 5, 300, 20, optionName, MinecraftClient.getInstance().textRenderer));
        this.addDrawableChild(new BooleanWidget(x + 320, y, 50, 20, object, field));
        this.addDrawableChild(new ResetButtonWidget(x + 380, y, 50, 20, object, field));
    }

    // FIXME: Refactor this into its own class
    public void createListWidgetWithReset(int x, int y, Text optionName, Object object, Field field) {
        this.addDrawableChild(new TextWidget(x, y - 5, 300, 20, optionName, MinecraftClient.getInstance().textRenderer));
        this.addDrawableChild(new ListWidget(x + 320, y, 50, 20, object, field));
        this.addDrawableChild(new ResetButtonWidget(x + 380, y, 50, 20, object, field));
    }

    // FIXME: Refactor this into its own class
    public void createIntegerWidgetWithReset(int x, int y, Text optionName, Object object, Field field) {
        this.addDrawableChild(new TextWidget(x, y - 5, 300, 20, optionName, MinecraftClient.getInstance().textRenderer));
        this.addDrawableChild(new IntegerWidget(x + 320, y, 50, 20, object, field));
        this.addDrawableChild(new ResetButtonWidget(x + 380, y, 50, 20, object, field));
    }
}
