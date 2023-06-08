package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.comparators.DuFileLexicographicalComparator;
import ru.nsu.fit.sckwo.comparators.DuFileSizeComparator;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;
import ru.nsu.fit.sckwo.exception.JduInvalidArgumentsException;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.min;

public class TreePrinter {
    private final JduOptions options;
    private final PrintStream printStream;
    private final Comparator<DuFile> comparator;
    private final FileSizeCacheCalculator fileSizeCacheCalculator;
    private String currentCompoundIndent = "";
    private final ArrayList<Integer> countsOfChildren;
    private static final Logger LOGGER = Logger.getLogger(TreePrinter.class.getName());

    private final ArrayList<Path> visited;

    private static final String INDENT_HORIZONTAL_BAR = "─";
    private static final String INDENT_TAB = "   ";

    public TreePrinter(JduOptions options, @NotNull PrintStream printStream) throws JduInvalidArgumentsException {
        this.options = options;
        this.printStream = printStream;
        try {
            countsOfChildren = new ArrayList<>(options.depth());
            fileSizeCacheCalculator = new FileSizeCacheCalculator(options.depth());
        } catch (OutOfMemoryError outOfMemoryError) {
            throw new JduInvalidArgumentsException("The value of the depth parameter is too large.", outOfMemoryError);
        }
        visited = new ArrayList<>();
        switch (options.comparatorType()) {
            case SIZE_COMPARATOR ->
                    comparator = new DuFileSizeComparator(fileSizeCacheCalculator::calculateSize).reversed();
            case LEXICOGRAPHICAL_COMPARATOR -> comparator = new DuFileLexicographicalComparator();
            case default -> throw new JduInvalidArgumentsException("Parameter not specified: comparator.");
        }
        try {
            Handler fileHandler = new FileHandler("log.txt");
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException ignored) {
        }
    }

    public void print(DuFile curFile, int curDepth) throws IOException {
        fileSizeCacheCalculator.setStartDepth(curDepth);
        printStream.println(
                currentCompoundIndent
                        + curFile.getPath().getFileName()
                        + " "
                        + formattedHumanReadableByteSize(curFile)
                        + "["
                        + curFile.getType().getName()
                        + "]");
        fileSizeCacheCalculator.removeCacheEntry(curFile.getPath());
        if (curDepth > 0 && curDepth < MAX_VALUE && curDepth >= options.depth()) {
            return;
        }
        switch (curFile.getType()) {
            case SYMLINK -> {
                if (options.followSymlinks()) {
                    if (visited.contains(curFile.getPath().toAbsolutePath())) {
                        return;
                    }
                    DuFile targetOfSymLink = new DuFile(Files.readSymbolicLink(curFile.getPath()));
                    visited.add(curFile.getPath().toAbsolutePath());
                    countsOfChildren.add(curDepth, 0);
                    updateCurrentCompoundIndent(curDepth + 1);
                    currentCompoundIndent = currentCompoundIndent.replace("─ ", "▷ ");
                    print(targetOfSymLink, curDepth + 1);
                    visited.clear();
                }
            }
            case DIRECTORY -> {
                try (Stream<Path> childrenFilesStream = Files.list(curFile.getPath())) {
                    List<Path> childrenFilesPaths = childrenFilesStream.toList();
                    ArrayList<DuFile> children = new ArrayList<>();
                    for (Path childFilePath : childrenFilesPaths) {
                        children.add(new DuFile(childFilePath));
                    }
                    children.sort(comparator);
                    int countOfFiles = min(children.size(), options.limit());
                    countsOfChildren.add(curDepth, countOfFiles);
                    children
                            .stream()
                            .skip(options.limit())
                            .forEach(child -> fileSizeCacheCalculator.removeCacheEntry(child.getPath()));
                    children
                            .stream()
                            .limit(countOfFiles)
                            .forEach(child -> {
                                countsOfChildren.set(curDepth, countsOfChildren.get(curDepth) - 1);
                                updateCurrentCompoundIndent(curDepth + 1);
                                try {
                                    print(child, curDepth + 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });

                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Unable to get access to the file: {0}", e.getMessage());
                }
            }
            case REGULAR_FILE, UNKNOWN_FORMAT_FILE -> countsOfChildren.add(curDepth, 0);
        }
    }

    private void updateCurrentCompoundIndent(int currentDepth) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < countsOfChildren.size(); i++) {
            if (i == currentDepth - 1) {
                if (countsOfChildren.get(i) == 0) {
                    builder.append("╰" + INDENT_HORIZONTAL_BAR + " ");
                } else {
                    builder.append("├" + INDENT_HORIZONTAL_BAR + " ");
                }
                break;
            }
            if (countsOfChildren.get(i) == 0) {
                builder.append(" " + INDENT_TAB);
            } else {
                builder.append("│" + INDENT_TAB);
            }

        }
        currentCompoundIndent = String.valueOf(builder);
    }

    private static String humanReadableByteCountBin(long fileSizeInBytes) {
        assert fileSizeInBytes >= 0;
        assert fileSizeInBytes < Long.MAX_VALUE;
        if (fileSizeInBytes < 1024) {
            return fileSizeInBytes + " " + FileSizeUnit.BYTE.getName();
        }
        final FileSizeUnit[] UNITS = FileSizeUnit.values();
        int digitGroups = (int) (Math.log(fileSizeInBytes) / Math.log(1024));
        double convertedSize = fileSizeInBytes / Math.pow(1024, digitGroups);
        FileSizeUnit currentUnit = UNITS[digitGroups];
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");
        return decimalFormat.format(convertedSize) + " " + currentUnit.getName();
    }

    private String formattedHumanReadableByteSize(DuFile file) {
        String formattedFileByteSize = "";
        if (file.getType() != DuFileType.UNKNOWN_FORMAT_FILE
                && file.getType() != DuFileType.BROKEN_SYMLINK
                && file.getType() != DuFileType.DANGLING_SYMLINK) {
            formattedFileByteSize = "[" + humanReadableByteCountBin(file.getSize(fileSizeCacheCalculator::calculateSize)) + "] ";
        }
        return formattedFileByteSize;
    }

}
