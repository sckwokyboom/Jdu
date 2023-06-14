package ru.nsu.fit.sckwo.dufile;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DuFile {
    private final Path absolutePath;
    private long size = -1;
    private DuFileType fileType;
    private final List<DuFile> children;


    public DuFile(@NotNull Path path, @NotNull DuFileType fileType) {
        this.absolutePath = path.toAbsolutePath();
        this.fileType = fileType;
        children = new ArrayList<>();
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

    public void setType(DuFileType type) {
        this.fileType = type;
    }

    @NotNull
    public List<DuFile> getChildren() {
        return children;
    }
}
