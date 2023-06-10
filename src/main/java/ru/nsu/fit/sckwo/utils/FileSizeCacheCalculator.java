package ru.nsu.fit.sckwo.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FileSizeCacheCalculator {
    private final LoadingCache<Path, Long> cache;
    private final int depthLimit;
    private int startDepth;

    public FileSizeCacheCalculator(int depthLimit) {
        this.depthLimit = depthLimit;
        cache = CacheBuilder.newBuilder()
                .maximumSize(1_000_000_000L)
                .expireAfterWrite(100, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public Long load(@NotNull Path absolutePath) {
                        return sizeOf(absolutePath);
                    }
                });
    }

    @NotNull
    public Long size(Path absoluteFilePath) {
        // CR: what happens if null?
        try {
            return cache.get(absoluteFilePath);
        } catch (ExecutionException e) {
            return 0L;
        }
    }

    public void removeCacheEntry(Path absoluteFilePathToRemove) {
        cache.invalidate(absoluteFilePathToRemove);
    }

    public void setStartDepth(int startDepth) {
        this.startDepth = startDepth;
    }

    public Long cacheEntriesSize() {
        return cache.size();
    }

    private static void requireExists(Path file) {
        Objects.requireNonNull(file, "directory");
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("File system element for parameter '" + "directory" + "' does not exist: '" + file + "'");
        }
    }

    private static void requireDirectory(Path directory) {
        Objects.requireNonNull(directory, "directory");
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Parameter '" + "directory" + "' is not a directory: '" + directory + "'");
        }
    }


    private long sizeOf(Path filePath) {
        Objects.requireNonNull(filePath, "path");
        // CR: check what happens with symlink
        if (Files.isDirectory(filePath)) {
            return sizeOfDirectory(filePath);
        } else {
            try {
                return Files.size(filePath);
            } catch (IOException ignored) {
            }
        }
        return 0;
    }


    private long sizeOfDirectory(Path directory) {
        Objects.requireNonNull(directory, "directory");
        try (Stream<Path> children = Files.list(directory)) {
            startDepth++;
            long size = children.mapToLong(file -> {
                long sizeOfFile = 0;
                if (!Files.isSymbolicLink(file)) {
                    try {
                        if (startDepth <= depthLimit) {
                            sizeOfFile = cache.get(file.toAbsolutePath());
                        } else {
                            sizeOfFile = sizeOf(file);
                        }
                    } catch (ExecutionException ignored) {
                    }
                }
                return sizeOfFile;
            }).sum();
            startDepth--;
            return size;
        } catch (IOException ignored) {
        }
        return 0;
    }
}
