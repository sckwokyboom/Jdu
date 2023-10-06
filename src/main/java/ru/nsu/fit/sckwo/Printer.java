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
    private int previousDepthLevel = -1;
    private final boolean followSymlinks;

    public Printer(@NotNull PrintStream printStream, int depthLimit, boolean followSymlinks) {
        this.printStream = printStream;
        countsOfChildren = new ArrayList<>(depthLimit);
        this.followSymlinks = followSymlinks;
    }

    /**
     * Prints information about the received file (its name, its size in human-readable units and its file type),
     * and also builds a special formatted indent illustrating the file tree from the accumulated information
     * when traversing the file tree and the recursion depth in the current file tree.
     * File can be one of:
     * <ul>
     *     <li>regular file</li>
     *     <li>directory</li>
     *     <li>symlink</li>
     *     <li>dangling symlink</li>
     *     <li>broken symlink</li>
     *     <li>unknown file (none of the above types)</li>
     * </ul>
     * <p/>
     * Possible corner cases:
     * <ul>
     *     <li>file size is negative (cases when the size of the file cannot be calculated, but its name exists) <br/>
     *     - do not show the size</li>
     *     <li>file is a direct child of a symlink - the indentation is marked with a special arrow</li>
     * </ul>
     * <p/>
     * Example of output:
     * <pre>
     * root [75 B] [directory]
     * ├─ dir1 [75 B] [directory]
     * │   ╰─ dirInside [75 B] [directory]
     * │       ├─ fileInside [75 B] [regular]
     * │       ╰─ link1 [0 B] [symlink]
     * ╰─ dir2 [0 B] [directory]
     *     ╰─ link2 [0 B] [symlink]
     *         ╰▷ dir1 [75 B] [directory]
     * </pre>
     */

    @Override
    public void visitFile(@NotNull DuFile curFile, int depthLevel) {
        if (depthLevel <= previousDepthLevel) {
            isParentSymlink = false;
        }
        if (depthLevel > 0) {
            decrementCountOfChildrenOnRecursionLevel(depthLevel - 1);
        }
        if (curFile.getType() == DuFileType.SYMLINK) {
            addCountOfChildrenOnRecursionLevel(depthLevel, 1);
            updateCurrentCompoundIndent(depthLevel);
            isParentSymlink = followSymlinks;
        } else {
            addCountOfChildrenOnRecursionLevel(depthLevel, curFile.getActualCountOfChildren());
            updateCurrentCompoundIndent(depthLevel);
            isParentSymlink = false;
        }
        printFileInfo(curFile);
        previousDepthLevel = depthLevel;
    }

    private void printFileInfo(@NotNull DuFile curFile) {
        printStream.println(
                currentCompoundIndent
                        + curFile.getAbsolutePath().getFileName().toString()
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