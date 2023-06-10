package ru.nsu.fit.sckwo.comparators;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.dufile.DuFile;

import java.util.Comparator;

public class DuFileLexicographicalComparator implements Comparator<DuFile> {

    @Override
    public int compare(@NotNull DuFile firstFile, @NotNull DuFile secondFile) {
        return CharSequence.compare(firstFile.getAbsolutePath().getFileName().toString(), secondFile.getAbsolutePath().getFileName().toString());
    }
}
