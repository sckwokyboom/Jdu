package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import ru.nsu.fit.sckwo.comparators.ComparatorType;
import ru.nsu.fit.sckwo.core.DuTest;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;
import ru.nsu.fit.sckwo.exception.JduException;
import ru.nsu.fit.sckwo.exception.JduRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrinterTest extends DuTest {
    private void testWithResult(JduOptions jduOptions, DuFile duFile, String answer) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try (PrintStream pos = new PrintStream(byteOutput)) {
            Printer printer = new Printer(jduOptions, pos);
            printHierarchy(printer, duFile, jduOptions, 0, new HashSet<>());
        } catch (JduException e) {
            throw new JduRuntimeException(e);
        }
        Assert.assertEquals("The results don't match:", answer, byteOutput.toString());
    }

    private static void printHierarchy(Printer printer, DuFile currentFile, JduOptions jduOptions, int depth, Set<DuFile> visited) {
        if (depth > jduOptions.depth()) {
            return;
        }
        printer.visitFile(currentFile, depth);
        printer.printFileInfo(currentFile);
        if (currentFile.getType() == DuFileType.SYMLINK && !jduOptions.followSymlinks()) {
            return;
        }
        if (currentFile.getType() == DuFileType.SYMLINK) {
            if (visited.contains(currentFile)) {
                return;
            }
            visited.add(currentFile);
        }
        List<DuFile> children = currentFile.getChildren();
        children.forEach(child -> printHierarchy(printer, child, jduOptions, depth + 1, visited));
    }

    private DuFile dir(String name, DuFile... children) {
        DuFile dir = new DuFile(Path.of(name), DuFileType.DIRECTORY);
        dir.getChildren().addAll(Arrays.stream(children).toList());
        dir.setSize(0);
        return dir;
    }

    private DuFile file(@NotNull String name) {
        DuFile file = new DuFile(Path.of(name), DuFileType.REGULAR_FILE);
        file.setSize(0);
        return file;
    }

    private DuFile symlink(@NotNull String symlinkName, @NotNull DuFile targetFile) {
        DuFile symlink = new DuFile(Path.of(symlinkName), DuFileType.SYMLINK);
        symlink.setSize(0);
        symlink.getChildren().add(targetFile);
        return symlink;
    }

    @Test
    public void fileRootTest() {

        DuFile fileRoot = file("root");
        JduOptions jduOptions = new JduOptions(
                true,
                256,
                256,
                ComparatorType.SIZE_COMPARATOR,
                fileRoot.getAbsolutePath());
        testWithResult(jduOptions, fileRoot, """
                root [0 B] [regular]\r
                                """);
    }

    @Test
    public void dirRootWithAllTypesOfFilesTest() {
        DuFile fileTargetOfSymlink = file("fileTargetOfSymlink");
        DuFile dirTargetOfSymlink = file("dirTargetOfSymlink");
        DuFile root = dir("dir0",
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
                    ComparatorType.SIZE_COMPARATOR,
                    root.getAbsolutePath());
            testWithResult(jduOptions, root, """
                    dir0 [0 B] [directory]\r
                    ├─ dir1 [0 B] [directory]\r
                    ├─ file [0 B] [regular]\r
                    ├─ symlinkToFile [0 B] [symlink]\r
                    │   ╰▷ fileTargetOfSymlink [0 B] [regular]\r
                    ├─ symlinkToDir [0 B] [symlink]\r
                    │   ╰▷ dirTargetOfSymlink [0 B] [regular]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ├─ test1 [0 B] [regular]\r
                        ╰─ test2 [0 B] [regular]\r
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
                    dir0 [0 B] [directory]\r
                    ├─ dir1 [0 B] [directory]\r
                    ├─ file [0 B] [regular]\r
                    ├─ symlinkToFile [0 B] [symlink]\r
                    ├─ symlinkToDir [0 B] [symlink]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ├─ test1 [0 B] [regular]\r
                        ╰─ test2 [0 B] [regular]\r
                                    """);
        }
    }

    @Test
    public void symlinkRootWithDirTest() {
        DuFile dir = dir("dir1", file("file1"), file("file2"));
        DuFile symlinkRoot = symlink("symlink", dir);
        JduOptions jduOptions = new JduOptions(
                true,
                256,
                256,
                ComparatorType.SIZE_COMPARATOR,
                symlinkRoot.getAbsolutePath());
        testWithResult(jduOptions, symlinkRoot, """
                symlink [0 B] [symlink]\r
                ╰▷ dir1 [0 B] [directory]\r
                    ├─ file1 [0 B] [regular]\r
                    ╰─ file2 [0 B] [regular]\r
                                """);
    }

    @Test
    public void loopSymlinkRootTest() {
        DuFile dir = dir("dir1", file("file1"), file("file2"));
        DuFile rootTarget = symlink("symlinkTarget", dir);
        DuFile symlinkRoot = symlink("symlink", rootTarget);
        dir.getChildren().add(symlinkRoot);

        {
            // followSymlinks = true
            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    symlinkRoot.getAbsolutePath());
            testWithResult(jduOptions, symlinkRoot, """
                    symlink [0 B] [symlink]\r
                    ╰▷ symlinkTarget [0 B] [symlink]\r
                        ╰▷ dir1 [0 B] [directory]\r
                            ├─ file1 [0 B] [regular]\r
                            ├─ file2 [0 B] [regular]\r
                            ╰─ symlink [0 B] [symlink]\r
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
                    symlink [0 B] [symlink]\r
                                    """);
        }
    }
}
