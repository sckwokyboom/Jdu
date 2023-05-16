package ru.nsu.fit.sckwo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.ToLongFunction;

public class DuFile {
    private final Path absolutePath;
    private long size = -1;

    public DuFile(Path absolutePath) {
        this.absolutePath = absolutePath;
    }

    public long getSize(ToLongFunction<Path> sizeOf) {
        if (size == -1
                && getType() != DuFileType.UNKNOWN_FORMAT_FILE
                && getType() != DuFileType.DANGLING_SYMLINK
                && getType() != DuFileType.BROKEN_SYMLINK) {
            size = sizeOf.applyAsLong(absolutePath);
//            size = FileUtils.sizeOf(absolutePath.toFile());
        }
        return size;
    }

    public Path getPath() {
        return absolutePath;
    }

    public DuFileType getType() {
        if (Files.isSymbolicLink(absolutePath)) {
            return checkSymlink();
        } else if (Files.isDirectory(absolutePath)) {
            return DuFileType.DIRECTORY;
        } else if (Files.isRegularFile(absolutePath)) {
            return DuFileType.REGULAR_FILE;
        } else {
            return DuFileType.UNKNOWN_FORMAT_FILE;
        }
    }

    private DuFileType checkSymlink() {
        try {
            if (absolutePath.toFile().getCanonicalFile().exists()) {
                return DuFileType.SYMLINK;
            } else {
//                System.out.println(absolutePath + " " + absolutePath.toFile().exists());
                return DuFileType.DANGLING_SYMLINK;
            }
        } catch (IOException e) {
            return DuFileType.UNKNOWN_FORMAT_FILE;
        }
    }
}
