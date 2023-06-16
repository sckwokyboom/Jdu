package ru.nsu.fit.sckwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.nsu.fit.sckwo.exception.JduInvalidArgumentsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Integer.MAX_VALUE;

public class JduOptionsParserTest {
    @Test
    public void defaultOptionsTest() {
        String[] args = new String[]{};
        Path rootPath = Path.of(System.getProperty("user.dir"));
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);

        Assertions.assertEquals(8, jduOptions.depth());
        Assertions.assertEquals(32, jduOptions.limit());
        Assertions.assertFalse(jduOptions.followSymlinks());
        Assertions.assertEquals(rootPath, jduOptions.rootAbsolutePath());
    }

    @Test
    public void depthOptionTest() {
        String[] args = new String[]{"-depth", "128"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);
        Assertions.assertEquals(128, jduOptions.depth());
    }

    @Test
    public void limitOptionTest() {
        String[] args = new String[]{"-limit", "128"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);
        Assertions.assertEquals(128, jduOptions.limit());
    }

    @Test
    public void followSymlinkOptionTest() {
        String[] args = new String[]{"-L"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);
        Assertions.assertTrue(jduOptions.followSymlinks());
    }

    @Test
    public void pathOptionTest() {
        String curDir = System.getProperty("user.dir");
        String[] args = new String[]{curDir};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);
        Assertions.assertEquals(curDir, jduOptions.rootAbsolutePath().toString());
    }

    @Test
    public void emptyParameterDepthOptionTest() {
        String[] args = new String[]{"-depth"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: Missing argument for option: depth";
        String actualMessage = thrown.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void notNumberParameterDepthOptionTest() {
        String[] args = new String[]{"-depth", "gsfg"};
        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"gsfg\" is not a correct number in option: depth";
        String actualMessage = thrown.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

    }

    @Test
    public void negativeNumberParameterDepthOptionTest() {
        String[] args = new String[]{"-depth", "-100000"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"" + -100000 + "\"" + " is not a positive number in option: depth";
        String actualMessage = thrown.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void emptyParameterLimitOptionTest() {
        String[] args = new String[]{"-limit"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: Missing argument for option: limit";
        String actualMessage = thrown.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void nonNumericParameterLimitOptionTest() {
        String[] args = new String[]{"-limit", "gdfsg"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"gdfsg\" is not a correct number in option: limit";
        String actualMessage = thrown.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void negativeNumberParameterLimitOptionTest() {
        String[] args = new String[]{"-limit", "-100000"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"" + -100000 + "\"" + " is not a positive number in option: limit";
        String actualMessage = thrown.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void correctPathParameterTest() throws IOException {
        Path tempFile = Files.createTempFile("test", ".txt");
        String[] args = new String[]{tempFile.toAbsolutePath().toString()};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduOptions jduOptions = jduOptionsParser.parseOptions(args);
        Assertions.assertEquals(tempFile, jduOptions.rootAbsolutePath());
    }

    @Test
    public void incorrectlyOptionTest() {
        String[] args1 = new String[]{"-limits", "124214"};
        String[] args2 = new String[]{"--depth", "123"};
        String[] args3 = new String[]{"---", "4321"};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown1 = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args1));
        JduInvalidArgumentsException thrown2 = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args2));
        JduInvalidArgumentsException thrown3 = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args3));

        String expectedMessage1 = "jdu: Unrecognized option: -limits";
        String actualMessage1 = thrown1.getMessage();

        String expectedMessage2 = "jdu: Unrecognized option: --depth";
        String actualMessage2 = thrown2.getMessage();

        String expectedMessage3 = "jdu: Unrecognized option: ---";
        String actualMessage3 = thrown3.getMessage();

        Assertions.assertEquals(actualMessage1, expectedMessage1);
        Assertions.assertEquals(actualMessage2, expectedMessage2);
        Assertions.assertEquals(actualMessage3, expectedMessage3);
    }

    @Test
    public void tooManyPathParametersTest() throws IOException {
        Path tempFile = Files.createTempFile("test", ".txt");
        Path tempFile2 = Files.createTempFile("test2", ".txt");
        String[] args = new String[]{tempFile.toAbsolutePath().toString(), tempFile2.toAbsolutePath().toString()};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));
        String expectedMessage = "jdu: Too many path parameters.";
        String actualMessage = thrown.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void toLargeParameterLimitOptionTest() {
        String[] args = new String[]{"-limit", String.valueOf(MAX_VALUE)};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"" + MAX_VALUE + "\"" + " is too large number in option: limit";
        String actualMessage = thrown.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void toLargeParameterDepthOptionTest() {
        String[] args = new String[]{"-depth", String.valueOf(MAX_VALUE)};

        JduOptionsParser jduOptionsParser = new JduOptionsParser();
        JduInvalidArgumentsException thrown = Assertions.assertThrows(
                JduInvalidArgumentsException.class,
                () -> jduOptionsParser.parseOptions(args));

        String expectedMessage = "jdu: \"" + MAX_VALUE + "\"" + " is too large number in option: depth";
        String actualMessage = thrown.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);
    }
}
