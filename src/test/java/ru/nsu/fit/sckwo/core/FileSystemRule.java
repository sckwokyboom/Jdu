package ru.nsu.fit.sckwo.core;

import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.FileSystem;

public class FileSystemRule implements BeforeEachCallback, AfterEachCallback {
    private FileSystem fileSystem;

    public FileSystem getFileSystem() {
        return this.fileSystem;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        fileSystem = Jimfs.newFileSystem();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        fileSystem.close();
    }
}
