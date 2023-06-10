package ru.nsu.fit.sckwo;

import org.junit.Assert;
import org.junit.Test;
import ru.nsu.fit.sckwo.utils.FileSizeUnit;

public class FileSizeUnitTest extends DuTest {
    @Test
    public void bytesToHumanReadableFormatTest() {
        long bytes1 = 0L;
        long bytes2 = 1L;
        long bytes3 = 1000L;
        long bytes4 = 1024L;
        long bytes5 = 2024000L;
        long bytes6 = 1_024_021L;
        long bytes7 = 1_021_252_154_021L;
        long bytes8 = Long.MAX_VALUE;
        Assert.assertEquals("0 B", FileSizeUnit.bytesToHumanReadableFormat(bytes1));
        Assert.assertEquals("1 B", FileSizeUnit.bytesToHumanReadableFormat(bytes2));
        Assert.assertEquals("1000 B", FileSizeUnit.bytesToHumanReadableFormat(bytes3));
        Assert.assertEquals("1 KB", FileSizeUnit.bytesToHumanReadableFormat(bytes4));
        Assert.assertEquals("1.93 MB", FileSizeUnit.bytesToHumanReadableFormat(bytes5));
        Assert.assertEquals("1 000.02 KB", FileSizeUnit.bytesToHumanReadableFormat(bytes6));
        Assert.assertEquals("951.12 GB", FileSizeUnit.bytesToHumanReadableFormat(bytes7));
        Assert.assertEquals("8 192 EB", FileSizeUnit.bytesToHumanReadableFormat(bytes8));
    }
}
