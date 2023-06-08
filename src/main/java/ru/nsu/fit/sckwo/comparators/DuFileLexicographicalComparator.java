package ru.nsu.fit.sckwo.comparators;

import ru.nsu.fit.sckwo.dufile.DuFile;

import java.util.Comparator;

public class DuFileLexicographicalComparator implements Comparator<DuFile> {

    @Override
    public int compare(DuFile firstFile, DuFile secondFile) {
        return CharSequence.compare(firstFile.getPath().getFileName().toString(), secondFile.getPath().getFileName().toString());
    }
}
