package ru.nsu.fit.sckwo.utils;

import ru.nsu.fit.sckwo.dufile.DuFileType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PrinterUtils {
    private static final String INDENT_TAB = "   ";

    // CR: move to units
    public static String bytesToHumanReadableFormat(long fileSizeInBytes) {
        assert fileSizeInBytes >= 0;
        if (fileSizeInBytes < 1024) {
            return fileSizeInBytes + " " + FileSizeUnit.BYTE.getName();
        }
        final FileSizeUnit[] UNITS = FileSizeUnit.values();
        int digitGroups = (int) (Math.log(fileSizeInBytes) / Math.log(1024));
        double convertedSize = fileSizeInBytes / Math.pow(1024, digitGroups);
        // CR: Math.min?
        FileSizeUnit currentUnit = UNITS[digitGroups];
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");
        return decimalFormat.format(convertedSize) + " " + currentUnit.getName();
    }

    public static String getCurrentCompoundIndent(int currentDepth, List<Integer> countsOfChildren, DuFileType duFileType) {
        StringBuilder builder = new StringBuilder();
        String INDENT_HORIZONTAL = "─";
        if (duFileType == DuFileType.SYMLINK) {
            INDENT_HORIZONTAL = "▷";
        }
        for (int i = 0; i < countsOfChildren.size(); i++) {
            if (i == currentDepth - 1) {
                if (countsOfChildren.get(i) == 0) {
                    builder.append("╰").append(INDENT_HORIZONTAL).append(" ");
                } else {
                    builder.append("├").append(INDENT_HORIZONTAL).append(" ");
                }
                break;
            }
            if (countsOfChildren.get(i) == 0) {
                builder.append(" " + INDENT_TAB);
            } else {
                builder.append("│" + INDENT_TAB);
            }

        }
        return String.valueOf(builder);
    }
}
