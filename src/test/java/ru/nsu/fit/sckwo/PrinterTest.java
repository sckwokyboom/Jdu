package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.nsu.fit.sckwo.comparators.ComparatorType;
import ru.nsu.fit.sckwo.core.DuFileWithChildren;
import ru.nsu.fit.sckwo.dufile.DuFileType;
import ru.nsu.fit.sckwo.exception.JduException;
import ru.nsu.fit.sckwo.exception.JduRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.nsu.fit.sckwo.core.DuFileHelper.*;

public class PrinterTest {
    private void testWithResult(@NotNull JduOptions jduOptions, @NotNull DuFileWithChildren duFile, @NotNull String answer) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try (PrintStream pos = new PrintStream(byteOutput, true, StandardCharsets.UTF_16)) {
            Printer printer = new Printer(pos, jduOptions.depth(), jduOptions.followSymlinks());
            printHierarchy(printer, duFile, jduOptions, 0, new HashSet<>());
        } catch (JduException e) {
            throw new JduRuntimeException(e);
        }
        Assertions.assertEquals(answer,
                byteOutput
                        .toString(StandardCharsets.UTF_16)
                        .replace("\r\n", "\n"),
                "The results don't match:");
    }

    private static void printHierarchy(@NotNull Printer printer, @NotNull DuFileWithChildren currentFile, @NotNull JduOptions jduOptions, int depth, @NotNull Set<Path> visited) {
        if (depth > jduOptions.depth()) {
            return;
        }
        printer.visitFile(currentFile, depth);
        if (currentFile.getType() == DuFileType.SYMLINK && !jduOptions.followSymlinks()) {
            return;
        }
        if (currentFile.getType() == DuFileType.SYMLINK) {
            if (visited.contains(currentFile.getAbsolutePath().getFileName())) {
                return;
            }
            visited.add(currentFile.getAbsolutePath().getFileName());
        }
        List<DuFileWithChildren> children = currentFile.getChildren();
        for (DuFileWithChildren child : children) {
            printHierarchy(printer, child, jduOptions, depth + 1, visited);
        }
    }

    @Test
    public void fileRootTest() {

        DuFileWithChildren fileRoot = file("root");
        JduOptions jduOptions = new JduOptions(
                true,
                256,
                256,
                ComparatorType.SIZE_COMPARATOR,
                fileRoot.getAbsolutePath());
        testWithResult(jduOptions, fileRoot, """
                root [0 B] [regular]
                """);
    }

    @Test
    public void dirRootWithAllTypesOfFilesTest() {
        DuFileWithChildren fileTargetOfSymlink = file("fileTargetOfSymlink");
        DuFileWithChildren dirTargetOfSymlink = file("dirTargetOfSymlink");
        DuFileWithChildren root = dir("dir0",
                dir("dir1"),
                file("file"),
                symlink("symlinkToFile", fileTargetOfSymlink),
                symlink("symlinkToDir", dirTargetOfSymlink),
                dir("dir2", file("test1"), file("test2")));

        {
            // followSymlinks = true
            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    root.getAbsolutePath());
            testWithResult(jduOptions, root, """
                    dir0 [0 B] [directory]
                    ├─ dir1 [0 B] [directory]
                    ├─ file [0 B] [regular]
                    ├─ symlinkToFile [0 B] [symlink]
                    │   ╰▷ fileTargetOfSymlink [0 B] [regular]
                    ├─ symlinkToDir [0 B] [symlink]
                    │   ╰▷ dirTargetOfSymlink [0 B] [regular]
                    ╰─ dir2 [0 B] [directory]
                        ├─ test1 [0 B] [regular]
                        ╰─ test2 [0 B] [regular]
                    """);
        }

        {
            // followSymlinks = false
            JduOptions jduOptions = new JduOptions(
                    false,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    root.getAbsolutePath());
            testWithResult(jduOptions, root, """
                    dir0 [0 B] [directory]
                    ├─ dir1 [0 B] [directory]
                    ├─ file [0 B] [regular]
                    ├─ symlinkToFile [0 B] [symlink]
                    ├─ symlinkToDir [0 B] [symlink]
                    ╰─ dir2 [0 B] [directory]
                        ├─ test1 [0 B] [regular]
                        ╰─ test2 [0 B] [regular]
                    """);
        }
    }

    @Test
    public void symlinkRootWithDirTest() {
        DuFileWithChildren dir = dir("dir1", file("file1"), file("file2"));
        DuFileWithChildren symlinkRoot = symlink("symlink", dir);
        JduOptions jduOptions = new JduOptions(
                true,
                256,
                256,
                ComparatorType.SIZE_COMPARATOR,
                symlinkRoot.getAbsolutePath());
        testWithResult(jduOptions, symlinkRoot, """
                symlink [0 B] [symlink]
                ╰▷ dir1 [0 B] [directory]
                    ├─ file1 [0 B] [regular]
                    ╰─ file2 [0 B] [regular]
                """);
    }

    @Test
    public void loopSymlinkRootTest() {
        DuFileWithChildren dir = dir("dir1", file("file1"), file("file2"));
        DuFileWithChildren rootTarget = symlink("symlinkTarget", dir);
        DuFileWithChildren symlinkRoot = symlink("symlink", rootTarget);
        dir.getChildren().add(symlinkRoot);
        dir.setActualCountOfChildren(3);

        {
            // followSymlinks = true
            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    symlinkRoot.getAbsolutePath());
            testWithResult(jduOptions, symlinkRoot, """
                    symlink [0 B] [symlink]
                    ╰▷ symlinkTarget [0 B] [symlink]
                        ╰▷ dir1 [0 B] [directory]
                            ├─ file1 [0 B] [regular]
                            ├─ file2 [0 B] [regular]
                            ╰─ symlink [0 B] [symlink]
                    """);
        }

        {
            // followSymlinks = false
            JduOptions jduOptions = new JduOptions(
                    false,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    symlinkRoot.getAbsolutePath());
            testWithResult(jduOptions, symlinkRoot, """
                    symlink [0 B] [symlink]
                    """);
        }
    }
}
