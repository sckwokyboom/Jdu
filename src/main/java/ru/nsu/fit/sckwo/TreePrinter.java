package ru.nsu.fit.sckwo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.comparators.DuFileLexicographicalComparator;
import ru.nsu.fit.sckwo.comparators.DuFileSizeComparator;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.exception.JduRuntimeException;
import ru.nsu.fit.sckwo.utils.FileSizeCacheCalculator;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.min;

public class TreePrinter {
    private final JduOptions options;
    private final Comparator<DuFile> comparator;
    private final FileSizeCacheCalculator fileSizeCacheCalculator;
    private final Printer printer;
    private static final Logger logger = LogManager.getLogger(TreePrinter.class);
    private final List<Path> visited;

    public TreePrinter(@NotNull JduOptions options, @NotNull PrintStream printStream) {
        printer = new Printer(options.depth(), printStream);
        this.options = options;
        fileSizeCacheCalculator = new FileSizeCacheCalculator(options.depth());
        visited = new ArrayList<>();
        switch (options.comparatorType()) {
            case SIZE_COMPARATOR -> comparator = new DuFileSizeComparator().reversed();
            case LEXICOGRAPHICAL_COMPARATOR -> comparator = new DuFileLexicographicalComparator();
            default -> {
                comparator = null;
                assert false;
            }
        }
    }

    public void print(@NotNull Path root) throws JduRuntimeException {
        try {
            print(new DuFile(root), 0);
        } catch (IOException e) {
            throw new JduRuntimeException(e);
        }
    }

    private void print(@NotNull DuFile curFile, int curDepth) throws IOException {
        fileSizeCacheCalculator.setStartDepth(curDepth);
        setSizeToFile(curFile);
        printer.printFileInfo(curFile);
        fileSizeCacheCalculator.removeCacheEntry(curFile.getAbsolutePath());
        if (curDepth >= options.depth() && curDepth < MAX_VALUE) {
            return;
        }
        switch (curFile.getType()) {
            case SYMLINK -> printSymlink(curFile, curDepth);
            case DIRECTORY -> printDirectory(curFile, curDepth);
            case default -> printer.addCountOfChildrenOnRecursionLevel(curDepth, 0);
        }
    }

    private void printSymlink(@NotNull DuFile curFile, int curDepth) {
        try {
            if (options.followSymlinks()) {
                if (visited.contains(curFile.getAbsolutePath().toAbsolutePath().normalize())) {
                    return;
                }
                visited.add(curFile.getAbsolutePath().toAbsolutePath().normalize());
                DuFile targetOfSymLink = new DuFile(Files.readSymbolicLink(curFile.getAbsolutePath()));
                printer.addCountOfChildrenOnRecursionLevel(curDepth, 0);
                printer.updateCurrentCompoundIndent(curFile, curDepth + 1);
                print(targetOfSymLink, curDepth + 1);
                visited.clear();
            }
        } catch (IOException e) {
            logger.error("Unable to get access to the file: {0}", e);
        }
    }

    private void printDirectory(@NotNull DuFile curFile, int curDepth) {
        try (Stream<Path> childrenFilesStream = Files.list(curFile.getAbsolutePath())) {
            List<Path> childrenFilesPaths = childrenFilesStream.toList();
            ArrayList<DuFile> children = new ArrayList<>();
            for (Path childFilePath : childrenFilesPaths) {
                DuFile child = new DuFile(childFilePath);
                setSizeToFile(child);
                children.add(child);
            }
            children.sort(comparator);
            int countOfFiles = min(children.size(), options.limit());
            printer.addCountOfChildrenOnRecursionLevel(curDepth, countOfFiles);
            children
                    .stream()
                    .skip(options.limit())
                    .forEach(child -> fileSizeCacheCalculator.removeCacheEntry(child.getAbsolutePath()));
            children
                    .stream()
                    .limit(countOfFiles)
                    .forEach(child -> {
                        printer.decrementCountOfChildrenOnCurrentRecursionLevel(curDepth);
                        printer.updateCurrentCompoundIndent(curFile, curDepth + 1);
                        try {
                            print(child, curDepth + 1);
                        } catch (IOException e) {
                            logger.error("Unable to get access to the file: {0}", e);
                        }
                    });

        } catch (IOException e) {
            logger.error("Unable to get access to the file: {0}", e);
        }
    }

    private void setSizeToFile(@NotNull DuFile curFile) {
        if (curFile.isFileSizeCountable()) {
            curFile.setSize(fileSizeCacheCalculator.size(curFile.getAbsolutePath()));
        }
    }
}
