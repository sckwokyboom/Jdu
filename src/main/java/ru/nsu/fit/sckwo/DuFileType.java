package ru.nsu.fit.sckwo;

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
}
