package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static ru.nsu.fit.sckwo.utils.FileSizeUnit.bytesToHumanReadableFormat;

public class Printer {
    private static final String INDENT_TAB = "   ";
    private final PrintStream printStream;
    private String currentCompoundIndent = "";
    private final List<Integer> countsOfChildren;

    public Printer(int depthLimit, @NotNull PrintStream printStream) {
        this.printStream = printStream;
        countsOfChildren = new ArrayList<>(depthLimit);
    }

    public void printFileInfo(DuFile curFile) {
        printStream.println(
                currentCompoundIndent
                        + curFile.getAbsolutePath().getFileName()
                        + " "
                        + getHumanReadableSizeOf(curFile)
                        + "["
                        + curFile.getType().getName()
                        + "]");

    }

    public void addCountOfChildrenOnRecursionLevel(int depthLevel, int countOfChildren) {
        countsOfChildren.add(depthLevel, countOfChildren);
    }

    public void decrementCountOfChildrenOnCurrentRecursionLevel(int depthLevel) {
        countsOfChildren.set(depthLevel, countsOfChildren.get(depthLevel) - 1);
    }

    public void updateCurrentCompoundIndent(DuFile curFile, int curDepth) {
        currentCompoundIndent = getCurrentCompoundIndent(curDepth, countsOfChildren, curFile.getType());
    }

    private static String getHumanReadableSizeOf(DuFile file) {
        String formattedFileByteSize = "";
        if (file.isFileSizeCountable()) {
            formattedFileByteSize = "[" + bytesToHumanReadableFormat(file.getSize()) + "] ";
        }
        return formattedFileByteSize;
    }

    public static String getCurrentCompoundIndent(int currentDepth, List<Integer> countsOfChildren, DuFileType duFileType) {
        StringBuilder builder = new StringBuilder();
        String INDENT_HORIZONTAL = "─";
        if (duFileType == DuFileType.SYMLINK) {
            INDENT_HORIZONTAL = "▷";
        }
        for (int i = 0; i < countsOfChildren.size(); i++) {
            if (i == currentDepth - 1) {
                if (countsOfChildren.get(i) == 0) {
                    builder.append("╰").append(INDENT_HORIZONTAL).append(" ");
                } else {
                    builder.append("├").append(INDENT_HORIZONTAL).append(" ");
                }
                break;
            }
            if (countsOfChildren.get(i) == 0) {
                builder.append(" " + INDENT_TAB);
            } else {
                builder.append("│" + INDENT_TAB);
            }

        }
        return String.valueOf(builder);
    }
}