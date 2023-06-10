package ru.nsu.fit.sckwo;

import org.apache.commons.cli.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.comparators.ComparatorType;
import ru.nsu.fit.sckwo.exception.JduInvalidArgumentsException;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import static java.lang.Integer.parseInt;

public class JduOptionsParser {
    final static Options options;
    static final int DEFAULT_DEPTH = 8;
    static final int DEFAULT_LIMIT = 32;
    static final int MAX_DEPTH = 2048;
    static final int MAX_LIMIT = 2048;

    static {
        options = new Options();
        options.addOption("depth", true, "Recursion depth.");
        options.addOption("limit", true, "Show n heaviest files.");
        options.addOption("L", false, "Follow symlinks.");
    }

    public JduOptions parseOptions(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.getArgList().size() > 1) {
                throw error("Too many path parameters.");
            }
            String filePath = "";
            if (cmd.getArgList().size() == 1) {
                filePath = cmd.getArgList().get(0);
            }
            if (cmd.getArgList().size() == 0) {
                filePath = System.getProperty("user.dir");
            }
            Path absolutePath = Path.of(filePath).toAbsolutePath();
            if (!Files.exists(absolutePath)) {
                throw error(absolutePath + " does not exist.");
            }
            if (!Files.isDirectory(absolutePath) && !Files.isRegularFile(absolutePath) && !Files.isSymbolicLink(absolutePath)) {
                throw error(absolutePath + " is not a file or a directory.");
            }
            int depth = parseNumericOption(cmd, "depth", MAX_DEPTH, DEFAULT_DEPTH);
            int limit = parseNumericOption(cmd, "limit", MAX_LIMIT, DEFAULT_LIMIT);
            return new JduOptions(cmd.hasOption("L"), depth, limit, ComparatorType.SIZE_COMPARATOR, absolutePath);
        } catch (InvalidPathException e) {
            throw error("Invalid file path:\n" + e.getMessage());
        } catch (ParseException e) {
            throw new JduInvalidArgumentsException(e.getMessage(), e);
        }
    }

    private int parseNumericOption(CommandLine cmd, String optionType, int limitValue, int defaultValue) throws JduInvalidArgumentsException {
        int optionValue = defaultValue;
        if (cmd.hasOption(optionType)) {
            try {
                optionValue = parseInt(cmd.getOptionValue(optionType));
            } catch (NumberFormatException e) {
                throw error("\"" + cmd.getOptionValue(optionType) + "\"" + " is not a correct number in option: " + optionType);
            }
            if (optionValue < 0) {
                throw error("\"" + optionValue + "\"" + " is not a positive number in option: " + optionType);
            }
            if (optionValue > limitValue) {
                throw error("\"" + optionValue + "\"" + " is too large number in option: " + optionType);
            }
        }
        return optionValue;
    }

    @Contract("_ -> new")
    private static @NotNull JduInvalidArgumentsException error(@NotNull String message) {
        return new JduInvalidArgumentsException(message);
    }

}
