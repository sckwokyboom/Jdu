package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static ru.nsu.fit.sckwo.dufile.DuFileType.isFileSizeCountable;
import static ru.nsu.fit.sckwo.utils.FileSizeUnit.bytesToHumanReadableFormat;

public class Printer implements FileVisitor {
    private static final String INDENT_TAB = "   ";
    private final PrintStream printStream;
    private String currentCompoundIndent = "";
    private final List<Integer> countsOfChildren;
    private boolean isParentSymlink = false;
    private final int depthLimit;
    private final boolean followSymlinks;

    public Printer(@NotNull JduOptions jduOptions, @NotNull PrintStream printStream) {
        this.printStream = printStream;
        countsOfChildren = new ArrayList<>(jduOptions.depth());
        depthLimit = jduOptions.depth();
        followSymlinks = jduOptions.followSymlinks();
    }

    @Override
    public void visitFile(@NotNull DuFile curFile, int depthLevel) {
        if (depthLevel > depthLimit || curFile.getType() == DuFileType.LOOP_SYMLINK) {
            isParentSymlink = false;
            return;
        }
        if (depthLevel > 0) {
            decrementCountOfChildrenOnRecursionLevel(depthLevel - 1);
        }
        if (curFile.getType() == DuFileType.SYMLINK) {
            addCountOfChildrenOnRecursionLevel(depthLevel, 1);
            updateCurrentCompoundIndent(depthLevel);
            isParentSymlink = followSymlinks;
        } else {
            addCountOfChildrenOnRecursionLevel(depthLevel, curFile.getChildren().size());
            updateCurrentCompoundIndent(depthLevel);
            isParentSymlink = false;
        }
        printFileInfo(curFile);
    }

    private void printFileInfo(@NotNull DuFile curFile) {
        printStream.println(
                currentCompoundIndent
                        + curFile.getAbsolutePath().getFileName()
                        + " "
                        + getHumanReadableSizeOf(curFile)
                        + "["
                        + curFile.getType().getName()
                        + "]");

    }

    private void addCountOfChildrenOnRecursionLevel(int depthLevel, int countOfChildren) {
        countsOfChildren.add(depthLevel, countOfChildren);
    }

    private void decrementCountOfChildrenOnRecursionLevel(int depthLevel) {
        countsOfChildren.set(depthLevel, countsOfChildren.get(depthLevel) - 1);
    }

    private void updateCurrentCompoundIndent(int curDepth) {
        currentCompoundIndent = getCompoundIndent(curDepth, countsOfChildren, isParentSymlink);
    }

    private static String getHumanReadableSizeOf(@NotNull DuFile file) {
        String formattedFileByteSize = "";
        if (isFileSizeCountable(file.getType())) {
            formattedFileByteSize = "[" + bytesToHumanReadableFormat(file.getSize()) + "] ";
        }
        return formattedFileByteSize;
    }

    private static String getCompoundIndent(int currentDepth, @NotNull List<Integer> countsOfChildren, boolean isParentSymlink) {
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