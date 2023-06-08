package ru.nsu.fit.sckwo.comparators;


import ru.nsu.fit.sckwo.dufile.DuFile;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.ToLongFunction;

public class DuFileSizeComparator implements Comparator<DuFile> {

    private final ToLongFunction<Path> sizeOf;

    public DuFileSizeComparator(ToLongFunction<Path> sizeOf) {
        this.sizeOf = sizeOf;
    }

    @Override
    public int compare(DuFile firstFile, DuFile secondFile) {
        return Long.compare(firstFile.getSize(sizeOf), secondFile.getSize(sizeOf));
    }
}
