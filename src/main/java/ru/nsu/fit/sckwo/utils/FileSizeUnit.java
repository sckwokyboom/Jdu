package ru.nsu.fit.sckwo.utils;

import java.text.DecimalFormat;

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
    },
    EXABYTE {
        public String getName() {
            return "EB";
        }
    };

    public abstract String getName();

    public static String bytesToHumanReadableFormat(long fileSizeInBytes) {
        assert fileSizeInBytes >= 0;
        if (fileSizeInBytes < 1024) {
            return fileSizeInBytes + " " + FileSizeUnit.BYTE.getName();
        }
        final FileSizeUnit[] UNITS = FileSizeUnit.values();
        int unitGroup = Math.min(UNITS.length - 1, (int) (Math.log(fileSizeInBytes) / Math.log(1024)));
        double convertedSize = fileSizeInBytes / Math.pow(1024, unitGroup);
        FileSizeUnit currentUnit = UNITS[unitGroup];
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");
        return decimalFormat.format(convertedSize).replace(",", ".") + " " + currentUnit.getName();
    }
}
