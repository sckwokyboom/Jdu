package ru.nsu.fit.sckwo.core;

import com.google.common.jimfs.Jimfs;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.nio.file.FileSystem;

class FileSystemRule implements TestRule {
    private FileSystem fileSystem;

    FileSystem getFileSystem() {
        return this.fileSystem;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                fileSystem = Jimfs.newFileSystem();
                try {
                    base.evaluate();
                } finally {
                    fileSystem.close();
                }
            }
        };
    }
}
