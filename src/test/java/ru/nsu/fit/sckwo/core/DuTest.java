package ru.nsu.fit.sckwo.core;

//import org.junit.Rule;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.FileSystem;

@ExtendWith(FileSystemRule.class)

public class DuTest {
    @RegisterExtension
    public final FileSystemRule fileSystemRule = new FileSystemRule();

    @NotNull
    public FileSystem fileSystem() {
        return fileSystemRule.getFileSystem();
    }

}
