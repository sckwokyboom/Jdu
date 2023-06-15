package ru.nsu.fit.sckwo;

import org.junit.Test;
import ru.nsu.fit.sckwo.comparators.ComparatorType;
import ru.nsu.fit.sckwo.core.DuTest;
import ru.nsu.fit.sckwo.dufile.DuFile;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ru.nsu.fit.sckwo.core.DuTreeElement.*;

public class TreeWalkerTest extends DuTest {

    private static DuFile traverse(JduOptions jduOptions) {
        TestVisitor visitor = new TestVisitor();
        TreeWalker walker = new TreeWalker(jduOptions, visitor);
        walker.walk(jduOptions.rootAbsolutePath());
        return visitor.root;
    }

    private static class TestVisitor implements FileVisitor {

        private DuFile root = null;

        private int prevDepth = -1;
        private DuFile curFile = null;

        private List<DuFile> level = new ArrayList<>();

        @Override
        public void visitFile(DuFile curFile, int depthLevel) {
            // CR: too lazy to finish
//            if (prevDepth == -1) {
//                root = curFile;
//                this.curFile = curFile;
//            } else if (prevDepth == depthLevel) {
//                this.curFile.getChildren().add(curFile);
//            } else if (prevDepth < depthLevel) {
//
//            }
//            prevDepth = depthLevel;
//            if ()
        }
    }

    @Test
    public void testOneDirOneRegularFile() throws IOException {
        FileSystem fs = fileSystem();
        Path dir = fs.getPath("dir");
        Files.createDirectory(dir);
        Path file = dir.resolve("file");
        Files.createFile(file);

        tree(fs, dir("dir", file("file")));
        traverse();
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
//        try (FileSystem fs = fileSystem()) {
//            Path rootPath = fs.getPath("root");
//
//            Files.createDirectory(rootPath);
//
//            Path dir1Path = rootPath.resolve("dir1");
//            Files.createDirectory(dir1Path);
//
//            Path dir2Path = rootPath.resolve("dir2");
//            Files.createDirectory(dir2Path);
//
//            Path regularFile1Path = dir1Path.resolve("file1");
//            Files.createFile(regularFile1Path);
//
//            Path regularFile3Path = dir1Path.resolve("file3");
//            Files.createFile(regularFile3Path);
//
//            Path regularFile2Path = dir2Path.resolve("file2");
//            Files.createFile(regularFile2Path);
//
//            Path regularFile4Path = dir2Path.resolve("file4");
//            Files.createFile(regularFile4Path);
//
//            JduOptions jduOptions = new JduOptions(
//                    true,
//                    256,
//                    256,
//                    ComparatorType.SIZE_COMPARATOR,
//                    rootPath.toAbsolutePath());
//            String[] result = {"root", "dir1", "file1", "file3", "dir2", "file2", "file4"};
//            traverse(jduOptions, result);
//        }
    }

}

