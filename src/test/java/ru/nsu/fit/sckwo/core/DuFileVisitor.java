package ru.nsu.fit.sckwo.core;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import ru.nsu.fit.sckwo.FileVisitor;
import ru.nsu.fit.sckwo.JduOptions;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;

import java.io.PrintStream;

import static ru.nsu.fit.sckwo.dufile.DuFileType.isFileSizeCountable;
import static ru.nsu.fit.sckwo.utils.FileSizeUnit.bytesToHumanReadableFormat;

public class DuFileVisitor implements FileVisitor {
    private final PrintStream printStream;
    private final int depthLimit;
    private int curDepth;
    private final String[] results;
    private int currentIndexInResults;


    public DuFileVisitor(@NotNull JduOptions jduOptions, @NotNull PrintStream printStream, String[] results) {
        this.printStream = printStream;
        depthLimit = jduOptions.depth();
        this.results = results;
    }

    private void printFileInfo(DuFile curFile) {
        Assert.assertEquals(results[currentIndexInResults], curFile.getAbsolutePath().getFileName().toString());
        currentIndexInResults++;
        printStream.println(
                " ".repeat(curDepth)
                        + curFile.getAbsolutePath().getFileName()
                        + " "
                        + getHumanReadableSizeOf(curFile)
                        + "["
                        + curFile.getType().getName()
                        + "]");
    }

    @Override
    public void visitFile(DuFile curFile, int depthLevel) {
        if (depthLevel > depthLimit || curFile.getType() == DuFileType.LOOP_SYMLINK) {
            curDepth = 0;
            return;
        }
        curDepth = depthLevel;
    }

    private static String getHumanReadableSizeOf(DuFile file) {
        String formattedFileByteSize = "";
        if (isFileSizeCountable(file.getType())) {
            formattedFileByteSize = "[" + bytesToHumanReadableFormat(file.getSize()) + "] ";
        }
        return formattedFileByteSize;
    }
}
