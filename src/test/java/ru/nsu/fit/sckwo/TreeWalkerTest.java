package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.nsu.fit.sckwo.comparators.ComparatorType;
import ru.nsu.fit.sckwo.core.DuFileWithChildren;
import ru.nsu.fit.sckwo.core.DuTest;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;
import ru.nsu.fit.sckwo.exception.JduException;
import ru.nsu.fit.sckwo.exception.JduRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import static ru.nsu.fit.sckwo.core.DuFileHelper.*;

public final class TreeWalkerTest extends DuTest {

    private static DuFileWithChildren traverse(@NotNull JduOptions jduOptions) {
        TestVisitor visitor = new TestVisitor();
        TreeWalker walker = new TreeWalker(jduOptions, visitor);
        walker.walk(jduOptions.rootAbsolutePath());
        return visitor.root;
    }

    private static void printDuFileTree(@NotNull DuFileWithChildren root, int curDepth, @NotNull PrintStream pos) {
        pos.println("  ".repeat(curDepth) + root.getAbsolutePath().getFileName());
        for (DuFileWithChildren child : root.getChildren()) {
            printDuFileTree(child, curDepth + 1, pos);
        }
    }

    private static String getInfoMessage(@NotNull DuFileWithChildren expected, @NotNull DuFileWithChildren actual) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try (PrintStream pos = new PrintStream(byteOutput)) {
            pos.println("\nExpected tree:");
            printDuFileTree(expected, 0, pos);
            pos.println("\nActual tree:");
            printDuFileTree(actual, 0, pos);
        } catch (JduException e) {
            throw new JduRuntimeException(e);
        }
        return byteOutput.toString();
    }

    private static class TestVisitor implements FileVisitor {
        private DuFileWithChildren root;

        private int prevDepth = 0;

        private final ArrayList<Deque<DuFileWithChildren>> allFiles = new ArrayList<>();


        @Override
        public void visitFile(DuFile curFile, int depthLevel) {
            DuFileWithChildren curFileWithChildren = new DuFileWithChildren(curFile.getAbsolutePath(), curFile.getType());
            if (depthLevel == 0) {
                Deque<DuFileWithChildren> level = new ArrayDeque<>();
                level.addLast(curFileWithChildren);
                allFiles.add(depthLevel, level);
                root = curFileWithChildren;
                return;
            }
            if (depthLevel > prevDepth) {
                Deque<DuFileWithChildren> level = new ArrayDeque<>();
                level.addLast(curFileWithChildren);
                allFiles.add(depthLevel, level);
                allFiles.get(prevDepth).getFirst().getChildren().add(curFileWithChildren);
            }
            if (depthLevel == prevDepth) {
                allFiles.get(depthLevel).addLast(curFileWithChildren);
                allFiles.get(depthLevel - 1).getFirst().getChildren().add(curFileWithChildren);
            }
            if (depthLevel < prevDepth) {
                allFiles.get(depthLevel).clear();
                allFiles.get(depthLevel).addLast(curFileWithChildren);
                allFiles.get(depthLevel - 1).getFirst().getChildren().add(curFileWithChildren);
                allFiles.get(prevDepth).clear();
            }
            prevDepth = depthLevel;
        }
    }

    @Test
    public void oneDirOneRegularFileTest() throws IOException {
        try (FileSystem fs = fileSystem()) {
            Path rootDir = fs.getPath("dir");
            Files.createDirectory(rootDir);
            Path file = rootDir.resolve("file");
            Files.createFile(file);
            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    rootDir.toAbsolutePath());
            DuFileWithChildren expected = dir("dir", file("file"));
            DuFileWithChildren actual = traverse(jduOptions);
            Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
        }
    }

    @Test
    public void oneDirManyRegularFileTest() throws IOException {
        try (FileSystem fs = fileSystem()) {
            Path rootDir = fs.getPath("dir");
            Files.createDirectory(rootDir);
            Path file1 = rootDir.resolve("file1");
            Files.createFile(file1);
            Path file2 = rootDir.resolve("file2");
            Files.createFile(file2);
            Path file3 = rootDir.resolve("file3");
            Files.createFile(file3);
            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    rootDir.toAbsolutePath());
            DuFileWithChildren expected = dir("dir", file("file1"), file("file2"), file("file3"));
            DuFileWithChildren actual = traverse(jduOptions);
            Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
        }
    }

    @Test
    public void loopSymlinkTest() throws IOException {
        try (FileSystem fs = fileSystem()) {
            Path dir = fs.getPath("dir");
            Files.createDirectory(dir);
            Path rootSymlink = fs.getPath("symlink");
            Files.createSymbolicLink(rootSymlink, dir);
            Path loopSymlink = dir.resolve("loopSymlink");
            Files.createSymbolicLink(loopSymlink, rootSymlink);

            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    rootSymlink.toAbsolutePath());

            DuFileWithChildren childOfSymlink = new DuFileWithChildren(Path.of("dir"), DuFileType.DIRECTORY);
            DuFileWithChildren loopSymlinkToRoot = new DuFileWithChildren(Path.of("loopSymlink"), DuFileType.SYMLINK);
            loopSymlinkToRoot.getChildren().add(new DuFileWithChildren(Path.of("symlink"), DuFileType.SYMLINK));
            childOfSymlink.getChildren().add(loopSymlinkToRoot);
            DuFileWithChildren expected = symlink("symlink", childOfSymlink);
            DuFileWithChildren actual = traverse(jduOptions);
            Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
        }
    }


    @Test
    public void printOnlyOneEmptyDirectoryTest() throws IOException {
        try (FileSystem fs = fileSystem()) {
            Path rootPath = fs.getPath("foo");
            Files.createDirectory(rootPath);

            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    rootPath.toAbsolutePath());

            DuFileWithChildren expected = dir("foo");
            DuFileWithChildren actual = traverse(jduOptions);
            Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
        }
    }

    @Test
    public void printOnlyOneFileTest() throws IOException {
        try (FileSystem fs = fileSystem()) {
            Path rootPath = fs.getPath("foo");
            Files.createFile(rootPath);

            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    rootPath.toAbsolutePath());

            DuFileWithChildren expected = file("foo");
            DuFileWithChildren actual = traverse(jduOptions);
            Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
        }
    }

    @Test
    public void oneSymlinkToDirTest() throws IOException {
        try (FileSystem fs = fileSystem()) {
            Path dir = fs.getPath("dir");
            Files.createDirectory(dir);
            Path rootSymlink = fs.getPath("symlink");
            Files.createSymbolicLink(rootSymlink, dir);
            Path file1 = dir.resolve("file1");
            Files.createFile(file1);
            Path file2 = dir.resolve("file2");
            Files.createFile(file2);
            Path file3 = dir.resolve("file3");
            Files.createFile(file3);

            // followSymlinks = true
            {
                JduOptions jduOptions = new JduOptions(
                        true,
                        256,
                        256,
                        ComparatorType.SIZE_COMPARATOR,
                        rootSymlink.toAbsolutePath());
                DuFileWithChildren expected = symlink("symlink", dir("dir", file("file1"), file("file2"), file("file3")));
                DuFileWithChildren actual = traverse(jduOptions);
                Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
            }

            // followSymlinks = false
            {
                JduOptions jduOptions = new JduOptions(
                        false,
                        256,
                        256,
                        ComparatorType.SIZE_COMPARATOR,
                        rootSymlink.toAbsolutePath());
                DuFileWithChildren expected = new DuFileWithChildren(Path.of("symlink"), DuFileType.SYMLINK);
                DuFileWithChildren actual = traverse(jduOptions);
                Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
            }
        }
    }


    @Test
    public void printTwoDirectoriesInLexicographicOrderTest() throws IOException {
        try (FileSystem fs = fileSystem()) {
            Path rootPath = fs.getPath("root");

            Files.createDirectory(rootPath);

            Path dir1Path = rootPath.resolve("dir1");
            Files.createDirectory(dir1Path);

            Path dir2Path = rootPath.resolve("dir2");
            Files.createDirectory(dir2Path);

            Path regularFile1Path = dir1Path.resolve("file1");
            Files.createFile(regularFile1Path);

            Path regularFile3Path = dir1Path.resolve("file3");
            Files.createFile(regularFile3Path);

            Path regularFile2Path = dir2Path.resolve("file2");
            Files.createFile(regularFile2Path);

            Path regularFile4Path = dir2Path.resolve("file4");
            Files.createFile(regularFile4Path);

            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    rootPath.toAbsolutePath());

            DuFileWithChildren expected = dir("root", dir("dir1", file("file1"), file("file3")), dir("dir2", file("file2"), file("file4")));
            DuFileWithChildren actual = traverse(jduOptions);
            Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
        }
    }

    @Test
    public void limitOptionTest() throws IOException {
        /*
          root
          ├─ dir
          │   ├─ file0.txt
          │   ├─ file1.txt
          │   ├─ file10.txt
          │   ├─ file11.txt
          │   ├─ file12.txt
          │   ├─ file13.txt
          │   ├─ file14.txt
          │   ├─ file15.txt
          │   ├─ file16.txt
          │   ╰─ file17.txt
          ├─ file1.txt
          ├─ file2.txt
          ├─ file3.txt
          ├─ file4.txt
          ├─ file5.txt
          ├─ file6.txt
          ├─ file7.txt
          ├─ file8.txt
          ╰─ file9.txt
*/
        try (FileSystem fs = fileSystem()) {
            Path rootPath = fs.getPath("root");

            Files.createDirectory(rootPath);
            for (int i = 1; i < 10; i++) {
                Path childrenPath = rootPath.resolve("file" + i + ".txt");
                Files.createFile(childrenPath);
            }
            Path dirPath = rootPath.resolve("dir");
            Files.createDirectory(dirPath);
            for (int i = 0; i < 25; i++) {
                Path childrenPath = dirPath.resolve("file" + i + ".txt");
                Files.createFile(childrenPath);
            }

            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    10,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            DuFileWithChildren expected = dir("root");
            DuFileWithChildren dir = dir("dir");
            dir.getChildren().add(file("file" + 0 + ".txt"));
            dir.getChildren().add(file("file" + 1 + ".txt"));
            dir.getChildren().add(file("file" + 10 + ".txt"));
            dir.getChildren().add(file("file" + 11 + ".txt"));
            dir.getChildren().add(file("file" + 12 + ".txt"));
            dir.getChildren().add(file("file" + 13 + ".txt"));
            dir.getChildren().add(file("file" + 14 + ".txt"));
            dir.getChildren().add(file("file" + 15 + ".txt"));
            dir.getChildren().add(file("file" + 16 + ".txt"));
            dir.getChildren().add(file("file" + 17 + ".txt"));
            expected.getChildren().add(dir);
            for (int i = 1; i < 10; i++) {
                DuFileWithChildren file = file("file" + i + ".txt");
                expected.getChildren().add(file);
            }
            DuFileWithChildren actual = traverse(jduOptions);
            Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
        }
    }

    @Test
    public void depthOptionTest() throws IOException {
        try (FileSystem fs = fileSystem()) {
            Path rootPath = fs.getPath("root");
            Files.createDirectory(rootPath);
            Path dirDepth1 = rootPath.resolve("dir1");
            Files.createDirectory(dirDepth1);
            Path dirDepth2 = dirDepth1.resolve("dir2");
            Files.createDirectory(dirDepth2);
            Path dirDepth3 = dirDepth2.resolve("dir3");
            Files.createDirectory(dirDepth3);
            Path dirDepth4 = dirDepth3.resolve("dir4");
            Files.createDirectory(dirDepth4);

            {
                // full depth
                JduOptions jduOptions = new JduOptions(
                        true,
                        256,
                        256,
                        ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                        rootPath.toAbsolutePath());

                DuFileWithChildren expected = dir("root", dir("dir1", dir("dir2", dir("dir3", dir("dir4")))));
                DuFileWithChildren actual = traverse(jduOptions);
                Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
            }

            {
                // depth = 0
                JduOptions jduOptions = new JduOptions(
                        true,
                        0,
                        256,
                        ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                        rootPath.toAbsolutePath());

                DuFileWithChildren expected = dir("root");
                DuFileWithChildren actual = traverse(jduOptions);
                Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
            }

            {
                // depth = 1
                JduOptions jduOptions = new JduOptions(
                        true,
                        1,
                        256,
                        ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                        rootPath.toAbsolutePath());

                DuFileWithChildren expected = dir("root", dir("dir1"));
                DuFileWithChildren actual = traverse(jduOptions);
                Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
            }

            {
                // depth = 2
                JduOptions jduOptions = new JduOptions(
                        true,
                        2,
                        256,
                        ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                        rootPath.toAbsolutePath());

                DuFileWithChildren expected = dir("root", dir("dir1", dir("dir2")));
                DuFileWithChildren actual = traverse(jduOptions);
                Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
            }

            {
                // depth = 3
                JduOptions jduOptions = new JduOptions(
                        true,
                        3,
                        256,
                        ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                        rootPath.toAbsolutePath());

                DuFileWithChildren expected = dir("root", dir("dir1", dir("dir2", dir("dir3"))));
                DuFileWithChildren actual = traverse(jduOptions);
                Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
            }

            {
                // depth = 4
                JduOptions jduOptions = new JduOptions(
                        true,
                        4,
                        256,
                        ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                        rootPath.toAbsolutePath());

                DuFileWithChildren expected = dir("root", dir("dir1", dir("dir2", dir("dir3", dir("dir4")))));
                DuFileWithChildren actual = traverse(jduOptions);
                Assertions.assertEquals(expected, actual, () -> getInfoMessage(expected, actual));
            }

        }
    }
}

