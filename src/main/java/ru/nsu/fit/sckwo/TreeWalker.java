package ru.nsu.fit.sckwo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.comparators.DuFileLexicographicalComparator;
import ru.nsu.fit.sckwo.comparators.DuFileSizeComparator;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;
import ru.nsu.fit.sckwo.exception.JduRuntimeException;
import ru.nsu.fit.sckwo.utils.FileSizeCacheCalculator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.min;

public class TreeWalker {
    private final JduOptions options;
    private final Comparator<DuFile> comparator;
    private final FileSizeCacheCalculator fileSizeCacheCalculator;
    private final Printer printer;
    private static final Logger logger = LogManager.getLogger(TreeWalker.class);
    private final List<Path> visited;

    public TreeWalker(@NotNull JduOptions options, @NotNull Printer printer) {
        this.printer = printer;
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

    public void walk(@NotNull Path root) throws JduRuntimeException {
        try {
            DuFile rootFile = new DuFile(root);
            walk(rootFile, 0, rootFile.getType() == DuFileType.SYMLINK);
        } catch (IOException e) {
            throw new JduRuntimeException(e);
        }
    }

    private void walk(@NotNull DuFile curFile, int curDepth, boolean isParentSymlink) throws IOException {
        fileSizeCacheCalculator.setStartDepth(curDepth);
        setSizeToFile(curFile);
        fileSizeCacheCalculator.removeCacheEntry(curFile.getAbsolutePath());
        if (curDepth > options.depth() && curDepth < MAX_VALUE) {
            return;
        }
        switch (curFile.getType()) {
            case SYMLINK -> walkSymlink(curFile, curDepth, isParentSymlink);
            case DIRECTORY -> walkDirectory(curFile, curDepth, isParentSymlink);
            case default -> {
                printer.visitFile(curDepth, 0, isParentSymlink);
                printer.printFileInfo(curFile);
            }
        }
    }

    private void walkSymlink(@NotNull DuFile curFile, int curDepth, boolean isParentSymlink) {
        printer.visitFile(curDepth, 1, isParentSymlink);
        printer.printFileInfo(curFile);
        try {
            if (options.followSymlinks()) {
                if (visited.contains(curFile.getAbsolutePath().toAbsolutePath().normalize())) {
                    return;
                }
                visited.add(curFile.getAbsolutePath().toAbsolutePath().normalize());
                DuFile targetOfSymLink = new DuFile(Files.readSymbolicLink(curFile.getAbsolutePath()));
                walk(targetOfSymLink, curDepth + 1, true);
                visited.clear();
            }
        } catch (IOException e) {
            logger.error("Unable to get access to the file: {0}", e);
        }
    }

    private void walkDirectory(@NotNull DuFile curFile, int curDepth, boolean isParentSymlink) {
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
            printer.visitFile(curDepth, countOfFiles, isParentSymlink);
            printer.printFileInfo(curFile);
            children
                    .stream()
                    .skip(options.limit())
                    .forEach(child -> fileSizeCacheCalculator.removeCacheEntry(child.getAbsolutePath()));
            children
                    .stream()
                    .limit(countOfFiles)
                    .forEach(child -> {
                        try {
                            walk(child, curDepth + 1, false);
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
