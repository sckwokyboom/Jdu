package ru.nsu.fit.sckwo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

    public void removeCacheEntry(Path absoluteFilePathToRemove) {
        cache.invalidate(absoluteFilePathToRemove);
    }

    @NotNull
    public Long calculateSize(Path absoluteFilePath) {
        try {
            return cache.get(absoluteFilePath);
        } catch (ExecutionException e) {
            return (long) 0;
        }
    }

    public Long cacheEntriesSize() {
        return cache.size();
    }

    public void setStartDepth(int startDepth) {
        this.startDepth = startDepth;
    }

    private static void requireExists(File file) {
        Objects.requireNonNull(file, "directory");
        if (!file.exists()) {
            throw new IllegalArgumentException("File system element for parameter '" + "directory" + "' does not exist: '" + file + "'");
        }
    }

    private static void requireDirectory(File directory) {
        Objects.requireNonNull(directory, "directory");
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter '" + "directory" + "' is not a directory: '" + directory + "'");
        }
    }


    private long sizeOf(Path filePath) {
        Objects.requireNonNull(filePath, "path");
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
            if (children == null) {
                startDepth--;
                return 0L;
            } else {
                long size = children.mapToLong(file -> {
                    long sizeOfFile = 0;
                    if (!Files.isSymbolicLink(file)) {
                        try {
                            if (startDepth <= depthLimit) {
                                sizeOfFile = cache.get(file.toAbsolutePath());
                            } else {
                                sizeOfFile = sizeOf(file);
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    return sizeOfFile;
                }).sum();
                startDepth--;
                return size;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
}
