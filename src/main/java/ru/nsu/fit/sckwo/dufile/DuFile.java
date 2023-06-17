package ru.nsu.fit.sckwo.dufile;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DuFile {
    private final Path absolutePath;
    private long size = -1;
    private DuFileType fileType;
    // CR: nChildren / childrenCount
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

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DuFile otherDuFile)) {
            return false;
        }

        return (this.absolutePath.getFileName().toString().equals(otherDuFile.absolutePath.getFileName().toString())
                && this.getChildren().equals(otherDuFile.getChildren())
                && this.getType() == otherDuFile.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileType, absolutePath.getFileName(), children);
    }
}
