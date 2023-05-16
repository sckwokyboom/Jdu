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

    public FileSizeCacheCalculator() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(1_000_000_000L)
                .expireAfterWrite(100, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public Long load(@NotNull Path file) {
                        return sizeOf(file.toFile());
                    }
                });
    }

    public long calculateSize(Path filePath) {
        try {
            return cache.get(filePath);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
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
        return file.isDirectory() ? sizeOfDirectory0(file) : file.length();
    }


    private long sizeOfDirectory0(File directory) {
        Objects.requireNonNull(directory, "directory");
        File[] files = directory.listFiles();
        if (files == null) {
            return 0L;
        } else {
            long size = 0L;

            for (File file : files) {
                if (!FileUtils.isSymlink(file)) {
                    long sizeOfFile = 0;
                    try {
                        sizeOfFile = cache.get(file.toPath());
//                        if (cache.size() % 100_000 == 0) {
//                            System.out.println(cache.size());
//                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    size += sizeOfFile;
                    if (size < 0L) {
                        break;
                    }
                }
            }

            return size;
        }
    }

//    public BigInteger sizeOfDirectoryAsBigInteger(File directory) {
//        return sizeOfDirectoryBig0(requireDirectoryExists(directory, "directory"));
//    }
//
//    private BigInteger sizeOfDirectoryBig0(File directory) {
//        Objects.requireNonNull(directory, "directory");
//        File[] files = directory.listFiles();
//        if (files == null) {
//            return BigInteger.ZERO;
//        } else {
//            BigInteger size = BigInteger.ZERO;
//
//            for (File file : files) {
//                if (!FileUtils.isSymlink(file)) {
//                    size = size.add(sizeOfBig0(file));
//                }
//            }
//
//            return size;
//        }
//    }
}
