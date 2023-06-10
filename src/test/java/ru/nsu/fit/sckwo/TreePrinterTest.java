package ru.nsu.fit.sckwo;

import org.junit.Assert;
import org.junit.Test;
import ru.nsu.fit.sckwo.comparators.ComparatorType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TreePrinterTest extends DuTest {

    public void testWithResult(JduOptions jduOptions, String answer) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try (PrintStream pos = new PrintStream(byteOutput)) {
            Jdu jdu = new Jdu(jduOptions, pos);
            jdu.printFileTree();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assert.assertEquals("The results don't match:", answer, byteOutput.toString());
    }

    @Test
    public void printFileInDirectoryTest() throws IOException {
        FileSystem fs = fileSystem();
        Path rootPath = fs.getPath("foo");
        Files.createDirectory(rootPath);
        Path childrenPath = rootPath.resolve("bar.txt");
        Files.createFile(childrenPath);

        JduOptions jduOptions = new JduOptions(
                true,
                256,
                256,
                ComparatorType.SIZE_COMPARATOR,
                rootPath.toAbsolutePath());

        testWithResult(jduOptions, """
                foo [0 B] [directory]\r
                ╰─ bar.txt [0 B] [regular]\r
                """);
    }

    @Test
    public void printOnlyOneDirectoryTest() throws IOException {
        FileSystem fs = fileSystem();
        Path rootPath = fs.getPath("foo");
        Files.createDirectory(rootPath);

        JduOptions jduOptions = new JduOptions(
                true,
                256,
                256,
                ComparatorType.SIZE_COMPARATOR,
                rootPath.toAbsolutePath());

        testWithResult(jduOptions, """
                foo [0 B] [directory]\r
                """);
    }

    @Test
    public void printOnlyOneFileTest() throws IOException {
        FileSystem fs = fileSystem();
        Path rootPath = fs.getPath("foo");
        Files.createFile(rootPath);

        JduOptions jduOptions = new JduOptions(
                true,
                256,
                256,
                ComparatorType.SIZE_COMPARATOR,
                rootPath.toAbsolutePath());

        testWithResult(jduOptions, """
                foo [0 B] [regular]\r
                """);
    }

    // CR: printOnlyOneSymlinkTest

    @Test
    public void printTwoDirectoriesInLexicographicOrderTest() throws IOException {
        //TODO: reversed?
        FileSystem fs = fileSystem();
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

        testWithResult(jduOptions, """
                root [0 B] [directory]\r
                ├─ dir1 [0 B] [directory]\r
                │   ├─ file1 [0 B] [regular]\r
                │   ╰─ file3 [0 B] [regular]\r
                ╰─ dir2 [0 B] [directory]\r
                    ├─ file2 [0 B] [regular]\r
                    ╰─ file4 [0 B] [regular]\r
                """);
    }

    @Test
    public void printSymlinkTest() throws IOException {
        FileSystem fs = fileSystem();
        Path rootPath = fs.getPath("root");
        Files.createDirectory(rootPath);
        Path dir1Path = rootPath.resolve("dir1");
        Files.createDirectory(dir1Path);
        Path filePath = dir1Path.resolve("file");
        Files.createFile(filePath);
        Path dir2Path = rootPath.resolve("dir2");
        Files.createDirectory(dir2Path);
        Path linkPath = dir2Path.resolve("link");
        Files.createSymbolicLink(linkPath, filePath);

        {
            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            testWithResult(jduOptions, """
                    root [0 B] [directory]\r
                    ├─ dir1 [0 B] [directory]\r
                    │   ╰─ file [0 B] [regular]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ╰─ link [0 B] [symlink]\r
                            ╰▷ file [0 B] [regular]\r
                    """);
        }

        {
            JduOptions jduOptions = new JduOptions(
                    false,
                    256,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            testWithResult(jduOptions, """
                    root [0 B] [directory]\r
                    ├─ dir1 [0 B] [directory]\r
                    │   ╰─ file [0 B] [regular]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ╰─ link [0 B] [symlink]\r
                    """);
        }
    }

    @Test
    public void printCycleSymlinkTest() throws IOException {
        FileSystem fs = fileSystem();
        Path rootPath = fs.getPath("root");
        Files.createDirectory(rootPath);
        Path dir1Path = rootPath.resolve("dir1");
        Files.createDirectory(dir1Path);
        Path dir2Path = rootPath.resolve("dir2");
        Files.createDirectory(dir2Path);
        Path link1Path = dir1Path.resolve("link1");
        Files.createSymbolicLink(link1Path, dir2Path);
        Path link2Path = dir2Path.resolve("link2");
        Files.createSymbolicLink(link2Path, dir1Path);

        {
            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            testWithResult(jduOptions, """
                    root [0 B] [directory]\r
                    ├─ dir1 [0 B] [directory]\r
                    │   ╰─ link1 [0 B] [symlink]\r
                    │       ╰▷ dir2 [0 B] [directory]\r
                    │           ╰─ link2 [0 B] [symlink]\r
                    │               ╰▷ dir1 [0 B] [directory]\r
                    │                   ╰─ link1 [0 B] [symlink]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ╰─ link2 [0 B] [symlink]\r
                            ╰▷ dir1 [0 B] [directory]\r
                                ╰─ link1 [0 B] [symlink]\r
                                    ╰▷ dir2 [0 B] [directory]\r
                                        ╰─ link2 [0 B] [symlink]\r
                    """);
        }

        {
            JduOptions jduOptions = new JduOptions(
                    false,
                    256,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            testWithResult(jduOptions, """
                    root [0 B] [directory]\r
                    ├─ dir1 [0 B] [directory]\r
                    │   ╰─ link1 [0 B] [symlink]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ╰─ link2 [0 B] [symlink]\r
                    """);
        }
    }

    //TODO: may be it is a bad idea???
    @Test
    public void edgeCaseLimitOptionTest() throws IOException {
        FileSystem fs = fileSystem();
        Path rootPath = fs.getPath("foo");
        Files.createDirectory(rootPath);
        Path childrenPath = rootPath.resolve("bar.txt");
        Files.createFile(childrenPath);
        Path linkPath = rootPath.resolve("link");
        Files.createSymbolicLink(linkPath, rootPath);

        JduOptions jduOptions = new JduOptions(
                true,
                256,
                Integer.MAX_VALUE,
                ComparatorType.SIZE_COMPARATOR,
                rootPath.toAbsolutePath());

        testWithResult(jduOptions, """
                foo [0 B] [directory]\r
                ├─ bar.txt [0 B] [regular]\r
                ╰─ link [0 B] [symlink]\r
                    ╰▷ foo [0 B] [directory]\r
                        ├─ bar.txt [0 B] [regular]\r
                        ╰─ link [0 B] [symlink]\r
                """);
    }

    @Test
    public void limitOptionTest() throws IOException {
        FileSystem fs = fileSystem();
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

        testWithResult(jduOptions, """
                root [0 B] [directory]\r
                ├─ dir [0 B] [directory]\r
                │   ├─ file0.txt [0 B] [regular]\r
                │   ├─ file1.txt [0 B] [regular]\r
                │   ├─ file10.txt [0 B] [regular]\r
                │   ├─ file11.txt [0 B] [regular]\r
                │   ├─ file12.txt [0 B] [regular]\r
                │   ├─ file13.txt [0 B] [regular]\r
                │   ├─ file14.txt [0 B] [regular]\r
                │   ├─ file15.txt [0 B] [regular]\r
                │   ├─ file16.txt [0 B] [regular]\r
                │   ╰─ file17.txt [0 B] [regular]\r
                ├─ file1.txt [0 B] [regular]\r
                ├─ file2.txt [0 B] [regular]\r
                ├─ file3.txt [0 B] [regular]\r
                ├─ file4.txt [0 B] [regular]\r
                ├─ file5.txt [0 B] [regular]\r
                ├─ file6.txt [0 B] [regular]\r
                ├─ file7.txt [0 B] [regular]\r
                ├─ file8.txt [0 B] [regular]\r
                ╰─ file9.txt [0 B] [regular]\r
                """);
    }

    @Test
    public void sizeFormatTest() throws IOException {
        FileSystem fs = fileSystem();
        Path rootPath = fs.getPath("root");
        Files.createDirectory(rootPath);

        Path dir1Path = rootPath.resolve("dir1");
        Files.createDirectory(dir1Path);

        Path dir2Path = rootPath.resolve("dir2");
        Files.createDirectory(dir2Path);

        Path regularFile1Path = dir1Path.resolve("file1");
        int file1SizeInBytes = 125_000;
        byte[] data1 = new byte[(int) file1SizeInBytes];
        Files.write(regularFile1Path, data1, StandardOpenOption.CREATE);

        Path regularFile3Path = dir1Path.resolve("file3");
        int file3SizeInBytes = 1_000_000;
        byte[] data3 = new byte[(int) file3SizeInBytes];
        Files.write(regularFile3Path, data3, StandardOpenOption.CREATE);


        Path regularFile2Path = dir2Path.resolve("file2");
        int file2SizeInBytes = 74_000_000;
        byte[] data2 = new byte[(int) file2SizeInBytes];
        Files.write(regularFile2Path, data2, StandardOpenOption.CREATE);

        Path regularFile4Path = dir2Path.resolve("file4");
        int file4SizeInBytes = 1_000_000_000;
        byte[] data4 = new byte[(int) file4SizeInBytes];
        Files.write(regularFile4Path, data4, StandardOpenOption.CREATE);

        JduOptions jduOptions = new JduOptions(
                true,
                256,
                256,
                ComparatorType.SIZE_COMPARATOR,
                rootPath.toAbsolutePath());

        testWithResult(jduOptions, """
                root [1 GB] [directory]\r
                ├─ dir2 [1 GB] [directory]\r
                │   ├─ file4 [953,67 MB] [regular]\r
                │   ╰─ file2 [70,57 MB] [regular]\r
                ╰─ dir1 [1,07 MB] [directory]\r
                    ├─ file3 [976,56 KB] [regular]\r
                    ╰─ file1 [122,07 KB] [regular]\r
                """);
    }

    @Test
    public void depthOptionTest() throws IOException {
        FileSystem fs = fileSystem();
        Path rootPath = fs.getPath("root");
        Files.createDirectory(rootPath);

        Path dir1Path = rootPath.resolve("dir1");
        Files.createDirectory(dir1Path);
        Path dirInside1Path = dir1Path.resolve("dirInside");
        Files.createDirectory(dirInside1Path);
        Path regularFileInside = dirInside1Path.resolve("fileInside");
        int fileSizeInBytes = 75;
        byte[] data = new byte[(int) fileSizeInBytes];
        Files.write(regularFileInside, data, StandardOpenOption.CREATE);

        Path dir2Path = rootPath.resolve("dir2");
        Files.createDirectory(dir2Path);

        Path linkToDir2 = dirInside1Path.resolve("link1");
        Files.createSymbolicLink(linkToDir2, dir2Path);

        Path linkToDir1 = dir2Path.resolve("link2");
        Files.createSymbolicLink(linkToDir1, dir1Path);

        // Test for full depth
        {
            JduOptions jduOptions = new JduOptions(
                    true,
                    256,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            testWithResult(jduOptions, """
                    root [75 B] [directory]\r
                    ├─ dir1 [75 B] [directory]\r
                    │   ╰─ dirInside [75 B] [directory]\r
                    │       ├─ fileInside [75 B] [regular]\r
                    │       ╰─ link1 [0 B] [symlink]\r
                    │           ╰▷ dir2 [0 B] [directory]\r
                    │               ╰─ link2 [0 B] [symlink]\r
                    │                   ╰▷ dir1 [75 B] [directory]\r
                    │                       ╰─ dirInside [75 B] [directory]\r
                    │                           ├─ fileInside [75 B] [regular]\r
                    │                           ╰─ link1 [0 B] [symlink]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ╰─ link2 [0 B] [symlink]\r
                            ╰▷ dir1 [75 B] [directory]\r
                                ╰─ dirInside [75 B] [directory]\r
                                    ├─ fileInside [75 B] [regular]\r
                                    ╰─ link1 [0 B] [symlink]\r
                                        ╰▷ dir2 [0 B] [directory]\r
                                            ╰─ link2 [0 B] [symlink]\r
                     """);
        }

        // Test for depth = 1
        {
            JduOptions jduOptions = new JduOptions(
                    true,
                    1,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            testWithResult(jduOptions, """
                    root [75 B] [directory]\r
                    ├─ dir1 [75 B] [directory]\r
                    ╰─ dir2 [0 B] [directory]\r
                    """);
        }

        // Test for depth = 2
        {
            JduOptions jduOptions = new JduOptions(
                    true,
                    2,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            testWithResult(jduOptions, """
                    root [75 B] [directory]\r
                    ├─ dir1 [75 B] [directory]\r
                    │   ╰─ dirInside [75 B] [directory]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ╰─ link2 [0 B] [symlink]\r
                    """);
        }

        // Test for depth = 3
        {
            JduOptions jduOptions = new JduOptions(
                    true,
                    3,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            testWithResult(jduOptions, """
                    root [75 B] [directory]\r
                    ├─ dir1 [75 B] [directory]\r
                    │   ╰─ dirInside [75 B] [directory]\r
                    │       ├─ fileInside [75 B] [regular]\r
                    │       ╰─ link1 [0 B] [symlink]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ╰─ link2 [0 B] [symlink]\r
                            ╰▷ dir1 [75 B] [directory]\r
                    """);
        }

        // Test for depth = 4
        {
            JduOptions jduOptions = new JduOptions(
                    true,
                    4,
                    256,
                    ComparatorType.LEXICOGRAPHICAL_COMPARATOR,
                    rootPath.toAbsolutePath());

            testWithResult(jduOptions, """
                    root [75 B] [directory]\r
                    ├─ dir1 [75 B] [directory]\r
                    │   ╰─ dirInside [75 B] [directory]\r
                    │       ├─ fileInside [75 B] [regular]\r
                    │       ╰─ link1 [0 B] [symlink]\r
                    │           ╰▷ dir2 [0 B] [directory]\r
                    ╰─ dir2 [0 B] [directory]\r
                        ╰─ link2 [0 B] [symlink]\r
                            ╰▷ dir1 [75 B] [directory]\r
                                ╰─ dirInside [75 B] [directory]\r
                    """);
        }
    }
}
