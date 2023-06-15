package ru.nsu.fit.sckwo;

import org.junit.Assert;
import org.junit.Test;
import ru.nsu.fit.sckwo.comparators.ComparatorType;
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

import static ru.nsu.fit.sckwo.core.FileSystemHelper.*;

public class TreeWalkerTest extends DuTest {

    private static DuFile traverse(JduOptions jduOptions) {
        TestVisitor visitor = new TestVisitor();
        TreeWalker walker = new TreeWalker(jduOptions, visitor);
        walker.walk(jduOptions.rootAbsolutePath());
        return visitor.root;
    }

    private static void printDuFileTree(DuFile root, int curDepth, PrintStream pos) {
        pos.println("  ".repeat(curDepth) + root.getAbsolutePath().getFileName());
        for (DuFile child : root.getChildren()) {
            printDuFileTree(child, curDepth + 1, pos);
        }
    }

    private static String getInfoMessage(DuFile expected, DuFile actual) {
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
        private DuFile root;

        @Override
        public void visitFile(DuFile curFile, int depthLevel) {
            if (depthLevel == 0) {
                root = curFile;
            }
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
            DuFile expected = dir("dir", file("file"));
            DuFile actual = traverse(jduOptions);
            String infoMsg = getInfoMessage(expected, actual);
            Assert.assertEquals(infoMsg, expected, actual);
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
            DuFile expected = dir("dir", file("file1"), file("file2"), file("file3"));
            DuFile actual = traverse(jduOptions);
            Assert.assertEquals(expected, actual);
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
            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.SIZE_COMPARATOR,
                    rootSymlink.toAbsolutePath());
            DuFile expected = symlink("symlink", dir("dir", file("file1"), file("file2"), file("file3")));
            DuFile actual = traverse(jduOptions);
            Assert.assertEquals(expected, actual);
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

            DuFile childOfSymlink = new DuFile(Path.of("dir"), DuFileType.DIRECTORY);
            DuFile loopSymlinkToRoot = new DuFile(Path.of("loopSymlink"), DuFileType.SYMLINK);
            loopSymlinkToRoot.getChildren().add(new DuFile(Path.of("symlink"), DuFileType.LOOP_SYMLINK));
            childOfSymlink.getChildren().add(loopSymlinkToRoot);
            DuFile expected = symlink("symlink", childOfSymlink);
            DuFile actual = traverse(jduOptions);
            String infoMsg = getInfoMessage(expected, actual);
            Assert.assertEquals(infoMsg, expected, actual);
        }
    }

}

