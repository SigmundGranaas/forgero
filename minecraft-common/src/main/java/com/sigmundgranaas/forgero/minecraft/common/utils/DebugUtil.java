package com.sigmundgranaas.forgero.minecraft.common.utils;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;

public class DebugUtil {
	public static boolean releaseControl() {
		GLFW.glfwSetInputMode(MinecraftClient.getInstance().getWindow().getHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		return true;
	}
}
