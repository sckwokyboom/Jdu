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
import static ru.nsu.fit.sckwo.dufile.DuFileType.isFileSizeCountable;
import static ru.nsu.fit.sckwo.dufile.DuFileType.recognizeFileType;

public class TreeWalker {
    private final JduOptions options;
    private final Comparator<DuFile> comparator;
    private final FileSizeCacheCalculator fileSizeCacheCalculator;
    private final FileVisitor visitor;
    private static final Logger logger = LogManager.getLogger(TreeWalker.class);
    private final List<Path> visited;

    public TreeWalker(@NotNull JduOptions options, @NotNull FileVisitor visitor) {
        this.visitor = visitor;
        this.options = options;
        fileSizeCacheCalculator = new FileSizeCacheCalculator(options.depth());
        visited = new ArrayList<>();
        comparator = switch (options.comparatorType()) {
            case SIZE_COMPARATOR -> new DuFileSizeComparator().reversed();
            case LEXICOGRAPHICAL_COMPARATOR -> new DuFileLexicographicalComparator();
        };
    }

    public void walk(@NotNull Path root) throws JduRuntimeException {
        try {
            DuFile rootFile = new DuFile(root, DuFileType.recognizeFileType(root));
            walk(rootFile, 0);
        } catch (IOException e) {
            throw new JduRuntimeException(e);
        }
    }

    private void walk(@NotNull DuFile curFile, int curDepth) throws IOException {
        fileSizeCacheCalculator.setStartDepth(curDepth);
        setSizeToFile(curFile);
        fileSizeCacheCalculator.removeCacheEntry(curFile.getAbsolutePath());
        // CR: MAX_VALUE?
        // CR: handle in printer
        if (curDepth > options.depth() && curDepth < MAX_VALUE) {
            visitor.visitFile(curFile, curDepth);
            return;
        }
        switch (curFile.getType()) {
            case SYMLINK -> walkSymlink(curFile, curDepth);
            case DIRECTORY -> walkDirectory(curFile, curDepth);
            case default -> visitor.visitFile(curFile, curDepth);
        }
    }

    private void walkSymlink(@NotNull DuFile symlink, int curDepth) {
        visitor.visitFile(symlink, curDepth);
        try {
            if (options.followSymlinks()) {
                Path absoluteSyminkPath = symlink.getAbsolutePath();
                if (visited.contains(absoluteSyminkPath.normalize())) {
                    symlink.setType(DuFileType.LOOP_SYMLINK);
                    visitor.visitFile(symlink, curDepth);
                    return;
                }
                visited.add(absoluteSyminkPath.normalize());
                Path targetOfSymlinkPath = Files.readSymbolicLink(absoluteSyminkPath);
                DuFile targetOfSymLink = new DuFile(targetOfSymlinkPath, recognizeFileType(targetOfSymlinkPath));
                symlink.getChildren().add(targetOfSymLink);
                walk(targetOfSymLink, curDepth + 1);
                visited.clear();
            }
        } catch (IOException e) {
            logger.error("Unable to get access to the file: {0}", e);
        }
    }

    private void walkDirectory(@NotNull DuFile curFile, int curDepth) {
        try (Stream<Path> childrenFilesStream = Files.list(curFile.getAbsolutePath())) {
            List<Path> childrenFilesPaths = childrenFilesStream.toList();
            ArrayList<DuFile> children = new ArrayList<>();
            for (Path childFilePath : childrenFilesPaths) {
                DuFile child = new DuFile(childFilePath, DuFileType.recognizeFileType(childFilePath));
                setSizeToFile(child);
                children.add(child);
            }
            children.sort(comparator);
            int countOfFiles = min(children.size(), options.limit());
            curFile.getChildren().addAll(children.subList(0, countOfFiles));
            visitor.visitFile(curFile, curDepth);
            int nChildren = options.limit();
            // clear the rest of the children
            for (int i = nChildren; i < children.size(); i++) {
                fileSizeCacheCalculator.removeCacheEntry(children.get(i).getAbsolutePath());
            }
            // visit first n
            for (int i = 0; i < nChildren; i++) {
                walk(children.get(i), curDepth + 1);
            }
        } catch (IOException e) {
            logger.error("Unable to get access to the file: {0}", e);
        }
    }

    private void setSizeToFile(@NotNull DuFile curFile) {
        if (isFileSizeCountable(curFile.getType())) {
            curFile.setSize(fileSizeCacheCalculator.size(curFile.getAbsolutePath()));
        }
    }
}
