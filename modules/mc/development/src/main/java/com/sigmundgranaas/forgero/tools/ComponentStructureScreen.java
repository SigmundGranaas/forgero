package com.sigmundgranaas.forgero.tools;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * A screen for displaying a detailed, scrollable report of a component's structure.
 */
public class ComponentStructureScreen extends Screen {
	private final String report;
	private final List<String> lines;
	private final Screen parent;
	private double scrollY = 0.0;
	private int topRow = 0;
	private static final int TEXT_COLOR = 0xFFFFFF;
	private static final int LINE_HEIGHT = 10;
	private static final int MARGIN = 20;

	public ComponentStructureScreen(String report, Screen parent) {
		super(Text.translatable("forgero.dev.inspector.title"));
		this.report = report;
		this.parent = parent;
		this.lines = List.of(report.split("\n"));
	}

	@Override
	protected void init() {
		super.init();
		// "Done" button
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.client.setScreen(this.parent))
				.position(this.width / 2 - 104, this.height - 28)
				.size(100, 20)
				.build());

		// "Copy to Clipboard" button
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("forgero.dev.inspector.copy"), button -> {
					MinecraftClient.getInstance().keyboard.setClipboard(this.report);
					if (MinecraftClient.getInstance().player != null) {
						MinecraftClient.getInstance().player.sendMessage(Text.translatable("forgero.dev.inspector.copied"), true);
					}
				})
				.position(this.width / 2 + 4, this.height - 28)
				.size(100, 20)
				.build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context);
		int y = MARGIN;
		int visibleLines = (this.height - MARGIN * 2) / LINE_HEIGHT;

		for (int i = 0; i < visibleLines && (topRow + i) < lines.size(); i++) {
			context.drawText(this.textRenderer, lines.get(topRow + i), MARGIN, y, TEXT_COLOR, false);
			y += LINE_HEIGHT;
		}

		// Title
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, TEXT_COLOR);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		scrollY -= amount * LINE_HEIGHT;
		clampScroll();
		return true;
	}

	private void clampScroll() {
		if (scrollY < 0) {
			scrollY = 0;
		}
		int maxScroll = Math.max(0, lines.size() * LINE_HEIGHT - (this.height - MARGIN * 2));
		if (scrollY > maxScroll) {
			scrollY = maxScroll;
		}
		this.topRow = (int) (scrollY / LINE_HEIGHT);
	}

	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(this.parent);
		}
	}
}
