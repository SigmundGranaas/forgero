package com.sigmundgranaas.forgero.model.texture.api;

import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;

import java.util.List;

/**
 * Orchestrates the entire texture generation process, executing a list of tasks.
 */
public interface TextureGenerator {
	/**
	 * Processes a list of texture generation tasks, creating and saving the resulting PNG files.
	 *
	 * @param tasks The list of tasks to execute.
	 */
	void generate(List<TextureGenerationTask> tasks);
}
