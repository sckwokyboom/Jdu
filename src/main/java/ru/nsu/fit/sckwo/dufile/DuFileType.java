package ru.nsu.fit.sckwo.dufile;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// CR: @Slf4j
public enum DuFileType {
    REGULAR_FILE {
        public String getName() {
            return "regular";
        }
    },
    DIRECTORY {
        public String getName() {
            return "directory";
        }
    },
    SYMLINK {
        public String getName() {
            return "symlink";
        }
    },
    BROKEN_SYMLINK {
        public String getName() {
            return "broken symlink";
        }
    },
    DANGLING_SYMLINK {
        public String getName() {
            return "dangling symlink";
        }
    },

    LOOP_SYMLINK {
        public String getName() {
            return "loop symlink";
        }
    },
    UNKNOWN_FORMAT_FILE {
        public String getName() {
            return "unknown file format";
        }
    };

    public abstract String getName();

    public static boolean isFileSizeCountable(@NotNull DuFileType fileType) {
        return fileType != DuFileType.UNKNOWN_FORMAT_FILE
                && fileType != DuFileType.BROKEN_SYMLINK
                && fileType != DuFileType.DANGLING_SYMLINK;
    }

    @NotNull
    public static DuFileType recognizeFileType(@NotNull Path absolutePath) {
        if (Files.isSymbolicLink(absolutePath)) {
            return recognizeTypeOfSymlink(absolutePath);
        } else if (Files.isDirectory(absolutePath)) {
            return DuFileType.DIRECTORY;
        } else if (Files.isRegularFile(absolutePath)) {
            return DuFileType.REGULAR_FILE;
        } else {
            return DuFileType.UNKNOWN_FORMAT_FILE;
        }
    }

    @NotNull
    private static DuFileType recognizeTypeOfSymlink(@NotNull Path absolutePath) {
        try {
            if (Files.exists(Files.readSymbolicLink(absolutePath))) {
                return DuFileType.SYMLINK;
            } else {
                // CR: can we reach this branch?
                return DuFileType.DANGLING_SYMLINK;
            }
        } catch (IOException e) {
            // CR: log
            return DuFileType.BROKEN_SYMLINK;
        }
    }
}
