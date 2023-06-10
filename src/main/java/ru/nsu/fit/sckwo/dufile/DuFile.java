package ru.nsu.fit.sckwo.dufile;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DuFile {
    private final Path absolutePath;
    private long size = -1;
    private final DuFileType fileType;

    public DuFile(@NotNull Path path) {
        this.absolutePath = path.toAbsolutePath();
        fileType = recognizeFileType();
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @NotNull
    public Path getAbsolutePath() {
        return absolutePath;
    }

    @NotNull
    public DuFileType getType() {
        return fileType;
    }

    @NotNull
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

    @NotNull
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
