package ru.nsu.fit.sckwo.comparators;


import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.dufile.DuFile;

import java.util.Comparator;

public class DuFileSizeComparator implements Comparator<DuFile> {

    @Override
    public int compare(@NotNull DuFile firstFile, @NotNull DuFile secondFile) {
        return Long.compare(firstFile.getSize(), secondFile.getSize());
    }
}
