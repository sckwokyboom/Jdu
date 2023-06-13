package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.dufile.DuFile;

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

    public void visitFile(int depthLevel, int countOfChildren, boolean isParenSymlink) {
        if (depthLevel > 0) {
            decrementCountOfChildrenOnRecursionLevel(depthLevel - 1);
        }
        addCountOfChildrenOnRecursionLevel(depthLevel, countOfChildren);
        updateCurrentCompoundIndent(depthLevel, isParenSymlink);
    }

    public void addCountOfChildrenOnRecursionLevel(int depthLevel, int countOfChildren) {
        countsOfChildren.add(depthLevel, countOfChildren);
    }

    public void decrementCountOfChildrenOnRecursionLevel(int depthLevel) {
        countsOfChildren.set(depthLevel, countsOfChildren.get(depthLevel) - 1);
    }

    public void updateCurrentCompoundIndent(int curDepth, boolean isParentSymlink) {
        currentCompoundIndent = getCurrentCompoundIndent(curDepth, countsOfChildren, isParentSymlink);
    }

    private static String getHumanReadableSizeOf(DuFile file) {
        String formattedFileByteSize = "";
        if (file.isFileSizeCountable()) {
            formattedFileByteSize = "[" + bytesToHumanReadableFormat(file.getSize()) + "] ";
        }
        return formattedFileByteSize;
    }

    public static String getCurrentCompoundIndent(int currentDepth, List<Integer> countsOfChildren, boolean isParentSymlink) {
        StringBuilder builder = new StringBuilder();
        String INDENT_HORIZONTAL = "─";
        if (isParentSymlink) {
            INDENT_HORIZONTAL = "▷";
        }
        for (int i = 0; i < countsOfChildren.size(); i++) {
            if (currentDepth == 0) {
                break;
            }
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