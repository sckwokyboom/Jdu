package ru.nsu.fit.sckwo.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
                    @NotNull
                    public Long load(@NotNull Path absolutePath) {
                        return sizeOf(absolutePath);
                    }
                });
    }

    @NotNull
    public Long size(@NotNull Path absoluteFilePath) {
        try {
            return cache.get(absoluteFilePath);
        } catch (ExecutionException e) {
            return -1L;
        }
    }

    public void removeCacheEntry(@NotNull Path absoluteFilePathToRemove) {
        cache.invalidate(absoluteFilePathToRemove);
    }

    public void setStartDepth(int startDepth) {
        this.startDepth = startDepth;
    }


    private long sizeOf(@NotNull Path filePath) {
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


    private long sizeOfDirectory(@NotNull Path directory) {
        try (Stream<Path> childrenFilesStream = Files.list(directory)) {
            startDepth++;
            List<Path> childrenFilesPaths = childrenFilesStream.toList();
            long size = 0;
            for (Path childPath : childrenFilesPaths) {
                try {
                    if (!Files.isSymbolicLink(childPath)) {
                        if (startDepth <= depthLimit) {
                            size += cache.get(childPath.toAbsolutePath());
                        } else {
                            size += sizeOf(childPath);
                        }
                    }
                } catch (ExecutionException ignored) {
                }
            }
            startDepth--;
            return size;
        } catch (IOException ignored) {
            return -1L;
        }
    }
}
