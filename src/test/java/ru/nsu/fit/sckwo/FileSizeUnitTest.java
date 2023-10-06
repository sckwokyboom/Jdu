package ru.nsu.fit.sckwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.nsu.fit.sckwo.utils.FileSizeUnit;

public class FileSizeUnitTest {
    @Test
    public void bytesToHumanReadableFormatTest() {
        final String nbsp = "\u00A0";
        long bytes1 = 0L;
        long bytes2 = 1L;
        long bytes3 = 1000L;
        long bytes4 = 1024L;
        long bytes5 = 2024000L;
        long bytes6 = 1_024_021L;
        long bytes7 = 1_021_252_154_021L;
        long bytes8 = Long.MAX_VALUE;
        Assertions.assertEquals("0" + nbsp + "B", FileSizeUnit.bytesToHumanReadableFormat(bytes1));
        Assertions.assertEquals("1" + nbsp + "B", FileSizeUnit.bytesToHumanReadableFormat(bytes2));
        Assertions.assertEquals("1000" + nbsp + "B", FileSizeUnit.bytesToHumanReadableFormat(bytes3));
        Assertions.assertEquals("1" + nbsp + "KB", FileSizeUnit.bytesToHumanReadableFormat(bytes4));
        Assertions.assertEquals("1.93" + nbsp + "MB", FileSizeUnit.bytesToHumanReadableFormat(bytes5));
        Assertions.assertEquals("1" + nbsp + "000.02" + nbsp + "KB", FileSizeUnit.bytesToHumanReadableFormat(bytes6));
        Assertions.assertEquals("951.12" + nbsp + "GB", FileSizeUnit.bytesToHumanReadableFormat(bytes7));
        Assertions.assertEquals("8" + nbsp + "192" + nbsp + "EB", FileSizeUnit.bytesToHumanReadableFormat(bytes8));
    }
}
