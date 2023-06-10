package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.comparators.DuFileLexicographicalComparator;
import ru.nsu.fit.sckwo.comparators.DuFileSizeComparator;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;
import ru.nsu.fit.sckwo.exception.JduInvalidArgumentsException;
import ru.nsu.fit.sckwo.utils.FileSizeCacheCalculator;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
import static ru.nsu.fit.sckwo.utils.PrinterUtils.bytesToHumanReadableFormat;
import static ru.nsu.fit.sckwo.utils.PrinterUtils.getCurrentCompoundIndent;

// CR: separate printing and traversing a tree
public class TreePrinter {
    private final JduOptions options;
    private final PrintStream printStream;
    private final Comparator<DuFile> comparator;
    private final FileSizeCacheCalculator fileSizeCacheCalculator;
    private String currentCompoundIndent = "";
    private final List<Integer> countsOfChildren;
    private static final Logger LOGGER = Logger.getLogger(TreePrinter.class.getName());

    private final List<Path> visited;

    public TreePrinter(JduOptions options, @NotNull PrintStream printStream) throws JduInvalidArgumentsException {
        this.options = options;
        this.printStream = printStream;
        try {
            countsOfChildren = new ArrayList<>(options.depth());
            fileSizeCacheCalculator = new FileSizeCacheCalculator(options.depth());
            // CR: do not catch, check depth beforehand
        } catch (OutOfMemoryError outOfMemoryError) {
            throw new JduInvalidArgumentsException("The value of the depth parameter is too large.", outOfMemoryError);
        }
        visited = new ArrayList<>();
        switch (options.comparatorType()) {
            case SIZE_COMPARATOR -> comparator = new DuFileSizeComparator(fileSizeCacheCalculator::size).reversed();
            case LEXICOGRAPHICAL_COMPARATOR -> comparator = new DuFileLexicographicalComparator();
            // CR: assert
            case default -> throw new JduInvalidArgumentsException("Parameter not specified: comparator.");
        }
        try {
            // CR: log4j
            Handler fileHandler = new FileHandler("log.txt");
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException ignored) {
        }
    }

    public void print(DuFile curFile) throws IOException {
        print(curFile, 0);
    }

    private void print(DuFile curFile, int curDepth) throws IOException {
        fileSizeCacheCalculator.setStartDepth(curDepth);
        printStream.println(
                currentCompoundIndent
                        + curFile.getPath().getFileName()
                        + " "
                        + getHumanReadableSizeOf(curFile)
                        + "["
                        + curFile.getType().getName()
                        + "]");
        fileSizeCacheCalculator.removeCacheEntry(curFile.getPath());
        if (curDepth >= options.depth() && curDepth < MAX_VALUE) {
            return;
        }
        switch (curFile.getType()) {
            case SYMLINK -> printSymlink(curFile, curDepth);
            case DIRECTORY -> printDirectory(curFile, curDepth);
            case default -> countsOfChildren.add(curDepth, 0);
        }
    }

    private void printSymlink(DuFile curFile, int curDepth) {
        try {
            if (options.followSymlinks()) {
                if (visited.contains(curFile.getPath().toAbsolutePath())) {
                    return;
                }
                DuFile targetOfSymLink = new DuFile(Files.readSymbolicLink(curFile.getPath()));
                visited.add(curFile.getPath().toAbsolutePath());
                countsOfChildren.add(curDepth, 0);
                currentCompoundIndent = getCurrentCompoundIndent(curDepth + 1, countsOfChildren, curFile.getType());
                print(targetOfSymLink, curDepth + 1);
                visited.clear();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to get access to the file: {0}", e.getMessage());
        }
    }

    private void printDirectory(DuFile curFile, int curDepth) {
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
                        currentCompoundIndent = getCurrentCompoundIndent(curDepth + 1, countsOfChildren, curFile.getType());
                        try {
                            print(child, curDepth + 1);
                        } catch (IOException e) {
                            LOGGER.log(Level.WARNING, "Unable to get access to the file: {0}", e.getMessage());
                        }
                    });

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to get access to the file: {0}", e.getMessage());
        }
    }

    private String getHumanReadableSizeOf(DuFile file) {
        String formattedFileByteSize = "";
        if (file.getType() != DuFileType.UNKNOWN_FORMAT_FILE
                && file.getType() != DuFileType.BROKEN_SYMLINK
                && file.getType() != DuFileType.DANGLING_SYMLINK) {
            formattedFileByteSize = "[" + bytesToHumanReadableFormat(file.getSize(fileSizeCacheCalculator::size)) + "] ";
        }
        return formattedFileByteSize;
    }
}
