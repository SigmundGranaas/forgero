package com.sigmundgranaas.forgero.minecraft.common.utils;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;

/**
 * Utility class for debugging purposes.
 */
public class DebugUtil {
	/**
	 * Will ensure the mouse is released when a debugger breakpoint is hit.
	 * Only needed in Linux environments.
	 * Place inside a breakpoint condition.
	 *
	 * @return true to let debugger breakpoints continue as usual.
	 */
	public static boolean releaseControl() {
		GLFW.glfwSetInputMode(MinecraftClient.getInstance().getWindow().getHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		return true;
	}
}
