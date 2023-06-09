package ru.nsu.fit.sckwo.utils;

public enum FileSizeUnit {
    BYTE {
        public String getName() {
            return "B";
        }
    },
    KILOBYTE {
        public String getName() {
            return "KB";
        }
    },
    MEGABYTE {
        public String getName() {
            return "MB";
        }
    },
    GIGABYTE {
        public String getName() {
            return "GB";
        }
    },
    TERABYTE {
        public String getName() {
            return "TB";
        }
    };

    public abstract String getName();
}
