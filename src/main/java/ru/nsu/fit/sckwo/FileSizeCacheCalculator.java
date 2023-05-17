package ru.nsu.fit.sckwo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
//                        System.out.println(absolutePath);
                        return sizeOf(absolutePath.toFile());
                    }
                });
    }

    public void removeCacheEntry(Path absoluteFilePathToRemove) {
        cache.invalidate(absoluteFilePathToRemove);
    }

    @NotNull
    public Long calculateSize(Path absoluteFilePath) {
        try {
//            System.out.println(absoluteFilePath);
            return cache.get(absoluteFilePath);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return (long) -1;
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


    private long sizeOf(File file) {
//        requireExists(file, "file");
        Objects.requireNonNull(file, "file");
        return file.isDirectory() ? sizeOfDirectory(file) : file.length();
    }


    private long sizeOfDirectory(File directory) {
        Objects.requireNonNull(directory, "directory");
        File[] files = directory.listFiles();
        startDepth++;
        if (files == null) {
            startDepth--;
            return 0L;
        } else {
            long size = 0L;
            for (File file : files) {
                if (!FileUtils.isSymlink(file)) {
                    long sizeOfFile = 0;
                    try {
                        if (startDepth <= depthLimit) {
                            sizeOfFile = cache.get(file.toPath().toAbsolutePath());
//                            System.out.println(file.toPath());
                        } else {
//                            System.out.println(startDepth);
                            sizeOfFile = sizeOf(file);
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    size += sizeOfFile;
                    if (size < 0L) {
                        break;
                    }
                }
            }
            startDepth--;
            return size;
        }
    }
}
