package ru.nsu.fit.sckwo;

import ru.nsu.fit.sckwo.comparators.DuFileLexicographicalComparator;
import ru.nsu.fit.sckwo.comparators.DuFileSizeComparator;
import ru.nsu.fit.sckwo.exception.JduInvalidArgumentsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
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
    private final Comparator<DuFile> comparator;
    private final FileSizeCacheCalculator fileSizeCacheCalculator;
    private String currentCompoundIndent = "";
    private final ArrayList<Integer> countsOfChildren;
    private static final Logger LOGGER = Logger.getLogger(TreePrinter.class.getName());

    private final ArrayList<Path> visited;

    private static final String INDENT_HORIZONTAL_BAR = "─";
    private static final String INDENT_TAB = "   ";

    public TreePrinter(JduOptions options) {
        this.options = options;
        countsOfChildren = new ArrayList<>(options.depth());
        fileSizeCacheCalculator = new FileSizeCacheCalculator(options.depth());
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
        } catch (IOException e) {
            System.err.println("Unable to create file for error logging.");
        }
    }

    public void print(DuFile curFile, int curDepth) throws IOException {
        fileSizeCacheCalculator.setStartDepth(curDepth);
        System.out.println(
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
                            .limit(options.limit())
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
                    LOGGER.log(Level.SEVERE, "Unable to get the list of children: {0}", e.getMessage());
                }
            }
            case REGULAR_FILE, UNKNOWN_FORMAT_FILE -> countsOfChildren.add(curDepth, 0);
        }
//        if (fileSizeCacheCalculator.cacheEntriesSize() < 300)
//            System.out.println(fileSizeCacheCalculator.cacheEntriesSize());
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

    private static String humanReadableByteCountBin(long bytes) {
        assert bytes > 0;
        assert bytes < Long.MAX_VALUE;
        long absBytesValue = Math.abs(bytes);
        if (absBytesValue < 1024) {
            return bytes + " B";
        }
        long value = absBytesValue;
        // CR: enum with sizes
        CharacterIterator prefixesOfSystemOfUnits = new StringCharacterIterator("KMGTPE");
        long numOfBytesNearTheExbibyte = 0xfffffffffffffffL;
        for (int i = 40; i >= 0 && absBytesValue > numOfBytesNearTheExbibyte >> i; i -= 10) {
            value >>= 10;
            prefixesOfSystemOfUnits.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, prefixesOfSystemOfUnits.current()).replace(',', '.');
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
