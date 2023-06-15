package ru.nsu.fit.sckwo;

import org.junit.Test;
import ru.nsu.fit.sckwo.comparators.ComparatorType;
import ru.nsu.fit.sckwo.core.DuFileVisitor;
import ru.nsu.fit.sckwo.core.DuTest;
import ru.nsu.fit.sckwo.exception.JduException;
import ru.nsu.fit.sckwo.exception.JduRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class TreeWalkerTest extends DuTest {

    public void testWithResult(JduOptions jduOptions, String[] answers) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try (PrintStream pos = new PrintStream(byteOutput)) {
            DuFileVisitor printer = new DuFileVisitor(jduOptions, pos, answers);
            TreeWalker treePrinter = new TreeWalker(jduOptions, printer);
            treePrinter.walk(jduOptions.rootAbsolutePath());
        } catch (JduException e) {
            throw new JduRuntimeException(e);
        }
    }


    /**
     * root [0 B] [directory]
     * ├─ dir1 [0 B] [directory]
     * │   ├─ file1 [0 B] [regular]
     * │   ╰─ file3 [0 B] [regular]
     * ╰─ dir2 [0 B] [directory]
     * ├─ file2 [0 B] [regular]
     * ╰─ file4 [0 B] [regular]
     */
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
            String[] result = {"root", "dir1", "file1", "file3", "dir2", "file2", "file4"};
            testWithResult(jduOptions, result);
        }
    }

}

