package ru.nsu.fit.sckwo.dufile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.ToLongFunction;

public class DuFile {
    private final Path absolutePath;
    private long size = -1;
    private final DuFileType fileType;

    public DuFile(Path absolutePath) {
        this.absolutePath = absolutePath;
        fileType = recognizeFileType();
    }

    public long getSize(ToLongFunction<Path> sizeOf) {
        if (size == -1
                && fileType != DuFileType.UNKNOWN_FORMAT_FILE
                && fileType != DuFileType.DANGLING_SYMLINK
                && fileType != DuFileType.BROKEN_SYMLINK) {
            size = sizeOf.applyAsLong(absolutePath);
        }
        return size;
    }

    public Path getPath() {
        return absolutePath;
    }

    public DuFileType getType() {
        return fileType;
    }

    private DuFileType recognizeFileType() {
        if (Files.isSymbolicLink(absolutePath)) {
            return recognizeTypeOfSymlink();
        } else if (Files.isDirectory(absolutePath)) {
            return DuFileType.DIRECTORY;
        } else if (Files.isRegularFile(absolutePath)) {
            return DuFileType.REGULAR_FILE;
        } else {
            return DuFileType.UNKNOWN_FORMAT_FILE;
        }
    }

    private DuFileType recognizeTypeOfSymlink() {
        try {
            if (Files.exists(Files.readSymbolicLink(absolutePath))) {
                return DuFileType.SYMLINK;
            } else {
                return DuFileType.DANGLING_SYMLINK;
            }
        } catch (IOException e) {
            return DuFileType.BROKEN_SYMLINK;
        }

    }
}
