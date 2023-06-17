package ru.nsu.fit.sckwo.core;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DuFileWithChildren extends DuFile {
    private final List<DuFileWithChildren> children;

    public DuFileWithChildren(@NotNull Path path, @NotNull DuFileType fileType) {
        super(path, fileType);
        children = new ArrayList<>();
    }

    public List<DuFileWithChildren> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DuFileWithChildren otherDuFile)) {
            return false;
        }

        return (getAbsolutePath().getFileName().toString().equals(otherDuFile.getAbsolutePath().getFileName().toString())
                && this.getChildren().equals(otherDuFile.getChildren())
                && this.getType() == otherDuFile.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getType(), this.getAbsolutePath().getFileName(), children);
    }
}
