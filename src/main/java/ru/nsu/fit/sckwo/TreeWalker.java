package ru.nsu.fit.sckwo;

import lombok.extern.slf4j.Slf4j;
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

import static java.lang.Integer.min;
import static ru.nsu.fit.sckwo.dufile.DuFileType.isFileSizeCountable;
import static ru.nsu.fit.sckwo.dufile.DuFileType.recognizeFileType;

@Slf4j

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

    /**
     * Traverses the file tree at the given path.
     * Uses the given interface <code>FileVisitor</code>, which processes each visited file.
     * <p/>
     * <code>FileVisitor</code>> receives the following information about a file:
     * its absolute path, its size, its file type, and the number of its direct children.
     * <p/>
     * File can be one of:
     * <ul>
     *     <li>regular file</li>
     *     <li>directory (with sub-files as tree children)</li>
     *     <li>symlink (the real path of a symlink is its own absolute path, not the path of its target)</li>
     *     <li>dangling symlink (symlink that points to a non-existent file)</li>
     *     <li>broken symlink (symlink whose path is no longer valid while the program is running)</li>
     *     <li>unknown file (none of the above types)</li>
     * </ul>
     * <p/>
     * Possible corner cases:
     * <ul>
     *     <li>cannot find file size - size is set to -1</li>
     *     <li>cannot traverse directory - do not traverse this directory</li>
     * </ul>
     * In all of this cases error is logged (but tree is still traversing).
     * <p/>
     * Cyclic symlinks are processed until the actual cycle occurs.
     * That is, the cycle will be detected when the same symlink gets processed twice by <code>FileVisitor</code>.
     * <br/>
     * For example, for this case:
     * <pre>
     * symlinkRoot [symlink]
     * ╰▷ symlinkTarget [symlink]
     *     ╰▷ dir [directory]
     *         ╰─ symlinkRoot [symlink]
     * </pre>
     * <code>FileVisitor</code> will receive for processing twice the first of the symlinks,
     * which will go in cycles in itself (<code>symlinkRoot</code>).
     */
    public void walk(@NotNull Path root) throws JduRuntimeException {
        try {
            DuFile rootFile = new DuFile(root, DuFileType.recognizeFileType(root));
            walk(rootFile, 0);
        } catch (IOException e) {
            throw new JduRuntimeException(e);
        }
    }

    private void walk(@NotNull DuFile curFile, int curDepth) throws IOException {
        assert (curDepth >= 0);
        fileSizeCacheCalculator.setStartDepth(curDepth);
        setSizeToFile(curFile);
        if (curDepth > options.depth()) {
            return;
        }
        switch (curFile.getType()) {
            case SYMLINK -> walkSymlink(curFile, curDepth);
            case DIRECTORY -> walkDirectory(curFile, curDepth);
            case default -> {
                visitor.visitFile(curFile, curDepth);
                fileSizeCacheCalculator.removeCacheEntry(curFile.getAbsolutePath());
            }
        }
    }

    private void walkSymlink(@NotNull DuFile symlink, int curDepth) {
        visitor.visitFile(symlink, curDepth);
        fileSizeCacheCalculator.removeCacheEntry(symlink.getAbsolutePath());
        try {
            if (options.followSymlinks()) {
                Path absoluteSyminkPath = symlink.getAbsolutePath();
                if (visited.contains(absoluteSyminkPath.normalize())) {
                    return;
                }
                visited.add(absoluteSyminkPath.normalize());
                Path targetOfSymlinkPath = Files.readSymbolicLink(absoluteSyminkPath);
                DuFile targetOfSymLink = new DuFile(targetOfSymlinkPath, recognizeFileType(targetOfSymlinkPath));
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
            int actualCountOfFiles = min(children.size(), options.limit());
            curFile.setActualCountOfChildren(actualCountOfFiles);
            visitor.visitFile(curFile, curDepth);
            for (int i = options.limit(); i < children.size(); i++) {
                fileSizeCacheCalculator.removeCacheEntry(children.get(i).getAbsolutePath());
            }
            for (int i = 0; i < actualCountOfFiles; i++) {
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
