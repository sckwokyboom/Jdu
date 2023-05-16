package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.comparators.ComparatorType;

import java.nio.file.Path;

public record JduOptions(boolean followSymlinks,
                         int depth, int limit,
                         @NotNull ComparatorType comparatorType,
                         @NotNull Path root) {

    @Override
    public boolean followSymlinks() {
        return followSymlinks;
    }

    @Override
    public int depth() {
        return depth;
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public ComparatorType comparatorType() {
        return comparatorType;
    }

    @Override
    public Path root() {
        return root;
    }
}
