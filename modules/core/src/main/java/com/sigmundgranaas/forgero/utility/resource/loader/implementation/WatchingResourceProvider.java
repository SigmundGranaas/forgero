package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceReloadNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * A {@link ResourceProvider} decorator that watches for filesystem changes and notifies listeners.
 * <p>
 * WatchingResourceProvider wraps a {@link FileSystemResourceProvider} and adds file watching
 * capabilities using Java's {@link WatchService}. When files are created, modified, or deleted,
 * registered listeners are notified, enabling hot-reload functionality during development.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * FileSystemResourceProvider baseProvider = new FileSystemResourceProvider(
 *     Path.of("src/main/resources"),
 *     "data"
 * );
 *
 * WatchingResourceProvider watchingProvider = new WatchingResourceProvider(baseProvider);
 *
 * // Register a reload listener
 * watchingProvider.addListener((path, type) -> {
 *     System.out.println("Resource " + type + ": " + path);
 *     // Trigger reload logic
 * });
 *
 * // Start watching (call once during initialization)
 * watchingProvider.startWatching();
 *
 * // Stop watching during shutdown
 * watchingProvider.stopWatching();
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This class is thread-safe. File watching runs on a separate daemon thread, and listeners
 * are stored in a thread-safe list.
 *
 * <h2>Performance Considerations</h2>
 * <p>
 * File watching is intended for development use only. In production, use the base
 * {@link FileSystemResourceProvider} without watching to avoid the overhead.
 *
 * @see FileSystemResourceProvider
 * @see ResourceReloadNotifier
 */
public class WatchingResourceProvider implements ResourceProvider, ResourceReloadNotifier, AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(WatchingResourceProvider.class);

	private final FileSystemResourceProvider delegate;
	private final List<ResourceReloadListener> listeners = new CopyOnWriteArrayList<>();
	private final AtomicBoolean watching = new AtomicBoolean(false);

	private WatchService watchService;
	private ExecutorService watchExecutor;

	/**
	 * Creates a new WatchingResourceProvider wrapping the given provider.
	 *
	 * @param delegate The FileSystemResourceProvider to wrap
	 */
	public WatchingResourceProvider(FileSystemResourceProvider delegate) {
		this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
	}

	// ========== File Watching Control ==========

	/**
	 * Starts watching the filesystem for changes.
	 * This method is idempotent - calling it multiple times has no effect.
	 *
	 * @throws IOException if the watch service cannot be created
	 */
	public synchronized void startWatching() throws IOException {
		if (watching.get()) {
			LOGGER.debug("Already watching, ignoring startWatching() call");
			return;
		}

		Path rootPath = delegate.getRootPath();
		if (!Files.exists(rootPath)) {
			LOGGER.warn("Cannot watch non-existent path: {}", rootPath);
			return;
		}

		watchService = FileSystems.getDefault().newWatchService();

		// Register all directories recursively
		registerDirectories(rootPath);

		// Start the watching thread
		watchExecutor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "WatchingResourceProvider-" + delegate.name());
			t.setDaemon(true);
			return t;
		});

		watching.set(true);
		watchExecutor.submit(this::watchLoop);

		LOGGER.info("Started watching for file changes in: {}", rootPath);
	}

	/**
	 * Stops watching the filesystem.
	 */
	public synchronized void stopWatching() {
		if (!watching.getAndSet(false)) {
			return;
		}

		if (watchExecutor != null) {
			watchExecutor.shutdownNow();
			watchExecutor = null;
		}

		if (watchService != null) {
			try {
				watchService.close();
			} catch (IOException e) {
				LOGGER.warn("Error closing watch service", e);
			}
			watchService = null;
		}

		LOGGER.info("Stopped watching for file changes");
	}

	/**
	 * Returns whether file watching is currently active.
	 *
	 * @return true if watching
	 */
	public boolean isWatching() {
		return watching.get();
	}

	// ========== ResourceReloadNotifier Implementation ==========

	@Override
	public void addListener(ResourceReloadListener listener) {
		Objects.requireNonNull(listener, "listener cannot be null");
		listeners.add(listener);
	}

	@Override
	public void removeListener(ResourceReloadListener listener) {
		listeners.remove(listener);
	}

	// ========== ResourceProvider Delegation ==========

	@Override
	public Set<String> getNamespaces() {
		return delegate.getNamespaces();
	}

	@Override
	public Stream<ResourcePath> list(ResourcePath path, boolean recursive, ResourceFilter filter) {
		return delegate.list(path, recursive, filter);
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return delegate.list(path, recursive);
	}

	@Override
	public Optional<InputStream> read(ResourcePath path) {
		return delegate.read(path);
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		return delegate.read(identifier);
	}

	@Override
	public boolean exists(ResourcePath path) {
		return delegate.exists(path);
	}

	@Override
	public int priority() {
		return delegate.priority();
	}

	@Override
	public String name() {
		return "Watching[" + delegate.name() + "]";
	}

	// ========== AutoCloseable ==========

	@Override
	public void close() {
		stopWatching();
	}

	// ========== Internal Watch Logic ==========

	/**
	 * Registers all directories for watching, recursively.
	 */
	private void registerDirectories(Path root) throws IOException {
		try (Stream<Path> paths = Files.walk(root)) {
			paths.filter(Files::isDirectory)
					.forEach(this::registerDirectory);
		}
	}

	/**
	 * Registers a single directory for watching.
	 */
	private void registerDirectory(Path dir) {
		try {
			dir.register(
					watchService,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY,
					StandardWatchEventKinds.ENTRY_DELETE
			);
			LOGGER.trace("Registered watch on: {}", dir);
		} catch (IOException e) {
			LOGGER.warn("Failed to register watch on: {}", dir, e);
		}
	}

	/**
	 * The main watch loop that processes filesystem events.
	 */
	private void watchLoop() {
		LOGGER.debug("Watch loop started");

		while (watching.get()) {
			WatchKey key;
			try {
				key = watchService.take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (ClosedWatchServiceException e) {
				break;
			}

			Path watchedDir = (Path) key.watchable();

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				if (kind == StandardWatchEventKinds.OVERFLOW) {
					LOGGER.warn("Watch event overflow for directory '{}' - some file change events may have been lost. " +
							"This can happen when many files change rapidly.", watchedDir);
					continue;
				}

				@SuppressWarnings("unchecked")
				WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
				Path relativePath = pathEvent.context();
				Path fullPath = watchedDir.resolve(relativePath);

				// If a new directory is created, register it for watching
				if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(fullPath)) {
					registerDirectory(fullPath);
				}

				// Convert to ResourcePath and notify listeners
				Optional<ResourcePath> resourcePath = toResourcePath(fullPath);
				if (resourcePath.isPresent()) {
					ChangeType changeType = toChangeType(kind);
					notifyListeners(resourcePath.get(), changeType);
				}
			}

			boolean valid = key.reset();
			if (!valid) {
				LOGGER.debug("Watch key no longer valid for: {}", watchedDir);
			}
		}

		LOGGER.debug("Watch loop ended");
	}

	/**
	 * Converts a filesystem path to a ResourcePath, if possible.
	 */
	private Optional<ResourcePath> toResourcePath(Path fullPath) {
		try {
			Path rootPath = delegate.getRootPath();
			String topLevel = delegate.getTopLevelDirectory();

			Path basePath = topLevel.isEmpty() ? rootPath : rootPath.resolve(topLevel);

			if (!fullPath.startsWith(basePath)) {
				return Optional.empty();
			}

			Path relativePath = basePath.relativize(fullPath);
			String relativeStr = relativePath.toString().replace('\\', '/');

			// Extract namespace (first path component)
			int firstSlash = relativeStr.indexOf('/');
			if (firstSlash == -1) {
				return Optional.empty(); // Just namespace directory, not a resource
			}

			String namespace = relativeStr.substring(0, firstSlash);
			String resourcePath = relativeStr.substring(firstSlash + 1);

			if (resourcePath.isEmpty()) {
				return Optional.empty();
			}

			return Optional.of(ResourcePath.fromIdentifier(new OpenIdentifier(namespace, resourcePath)));
		} catch (Exception e) {
			LOGGER.trace("Could not convert path to ResourcePath: {}", fullPath, e);
			return Optional.empty();
		}
	}

	/**
	 * Converts a WatchEvent kind to a ChangeType.
	 */
	private ChangeType toChangeType(WatchEvent.Kind<?> kind) {
		if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
			return ChangeType.CREATED;
		} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
			return ChangeType.MODIFIED;
		} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
			return ChangeType.DELETED;
		}
		return ChangeType.MODIFIED; // Default fallback
	}

	/**
	 * Notifies all registered listeners of a resource change.
	 */
	private void notifyListeners(ResourcePath path, ChangeType type) {
		LOGGER.debug("Resource {}: {}", type, path);

		for (ResourceReloadListener listener : listeners) {
			try {
				listener.onResourceChanged(path, type);
			} catch (Exception e) {
				LOGGER.error("Error in reload listener for {}: {}", path, e.getMessage(), e);
			}
		}
	}
}
