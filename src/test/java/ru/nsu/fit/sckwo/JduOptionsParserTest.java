package ru.nsu.fit.sckwo;

import org.junit.Assert;
import org.junit.Test;
import ru.nsu.fit.sckwo.exception.JduInvalidArgumentsException;

import java.io.File;
import java.nio.file.Path;

public class JduOptionsParserTest extends DuTest {
    @Test
    public void defaultOptionsTest() {
        String[] args = new String[]{};
        Path rootPath = Path.of(System.getProperty("user.dir"));
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);

        Assert.assertEquals(8, jduOptions.depth());
        Assert.assertEquals(32, jduOptions.limit());
        Assert.assertFalse(jduOptions.followSymlinks());
        Assert.assertEquals(rootPath, jduOptions.rootAbsolutePath());
    }

    @Test
    public void depthOptionTest() {
        String[] args = new String[]{"-depth", "128"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);
        Assert.assertEquals(128, jduOptions.depth());
    }

    @Test
    public void limitOptionTest() {
        String[] args = new String[]{"-limit", "128"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);
        Assert.assertEquals(128, jduOptions.limit());
    }

    @Test
    public void followSymlinkOptionTest() {
        String[] args = new String[]{"-L"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);
        Assert.assertTrue(jduOptions.followSymlinks());
    }

    @Test
    public void pathOptionTest() {
        String curDir = System.getProperty("user.dir");
        String[] args = new String[]{curDir};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);
        Assert.assertEquals(curDir, jduOptions.rootAbsolutePath().toString());
    }

    @Test
    public void emptyParameterDepthOptionTest() {
        String[] args = new String[]{"-depth"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: Missing argument for option: depth";
        String actualMessage = thrown.getMessage();

        Assert.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void notNumberParameterDepthOptionTest() {
        String[] args = new String[]{"-depth", "gsfg"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"gsfg\" is not a number in option: depth";
        String actualMessage = thrown.getMessage();

        Assert.assertEquals(expectedMessage, actualMessage);

    }

    @Test
    public void negativeNumberParameterDepthOptionTest() {
        String[] args = new String[]{"-depth", "-100000"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"" + -100000 + "\"" + " is not a positive number in option: depth";
        String actualMessage = thrown.getMessage();

        Assert.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void emptyParameterLimitOptionTest() {
        String[] args = new String[]{"-limit"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: Missing argument for option: limit";
        String actualMessage = thrown.getMessage();

        Assert.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void nonNumericParameterLimitOptionTest() {
        String[] args = new String[]{"-limit", "gdfsg"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"gdfsg\" is not a number in option: limit";
        String actualMessage = thrown.getMessage();

        Assert.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void negativeNumberParameterLimitOptionTest() {
        String[] args = new String[]{"-limit", "-100000"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"" + -100000 + "\"" + " is not a positive number in option: limit";
        String actualMessage = thrown.getMessage();

        Assert.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void incorrectlyPathEntered() {
        String[] args = new String[]{"fsd"};
        String curDir = System.getProperty("user.dir");
        String summaryPath = curDir + File.separator + "fsd";

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));
        String expectedMessage = "jdu: " + summaryPath + " does not exist.";
        String actualMessage = thrown.getMessage();

        Assert.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void incorrectlyOptionTest() {
        String[] args1 = new String[]{"-limits", "124214"};
        String[] args2 = new String[]{"--depth", "123"};
        String[] args3 = new String[]{"---", "4321"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown1 = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args1));
        JduInvalidArgumentsException thrown2 = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args2));
        JduInvalidArgumentsException thrown3 = Assert.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args3));

        String expectedMessage1 = "jdu: Unrecognized option: -limits";
        String actualMessage1 = thrown1.getMessage();

        String expectedMessage2 = "jdu: Unrecognized option: --depth";
        String actualMessage2 = thrown2.getMessage();

        String expectedMessage3 = "jdu: Unrecognized option: ---";
        String actualMessage3 = thrown3.getMessage();

        Assert.assertEquals(actualMessage1, expectedMessage1);
        Assert.assertEquals(actualMessage2, expectedMessage2);
        Assert.assertEquals(actualMessage3, expectedMessage3);
    }
}
