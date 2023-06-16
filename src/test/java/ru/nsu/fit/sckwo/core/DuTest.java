package ru.nsu.fit.sckwo.core;

//import org.junit.Rule;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.FileSystem;

@ExtendWith(FileSystemRule.class)

public class DuTest {
    @RegisterExtension
    public final FileSystemRule fileSystemRule = new FileSystemRule();

    public FileSystem fileSystem() {
        return fileSystemRule.getFileSystem();
    }

}
