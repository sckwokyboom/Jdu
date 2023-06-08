package ru.nsu.fit.sckwo;

import org.apache.commons.cli.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.comparators.ComparatorType;
import ru.nsu.fit.sckwo.exception.JduInvalidArgumentsException;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Integer.parseInt;

public class JduOptionsParser {
    final static Options options;
    static final int DEFAULT_DEPTH = 8;
    static final int DEFAULT_LIMIT = 32;

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
                throw error("Missing arguments.");
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
            int depth = DEFAULT_DEPTH, limit = DEFAULT_LIMIT;
            if (cmd.hasOption("depth")) {
                try {
                    depth = parseInt(cmd.getOptionValue("depth"));
                } catch (NumberFormatException e) {
                    throw error("\"" + cmd.getOptionValue("depth") + "\"" + " is not a number in option: depth");
                }

                if (depth < 0) {
                    throw error("\"" + depth + "\"" + " is not a positive number in option: depth");
                }
            }
            if (cmd.hasOption("limit")) {
                try {
                    limit = parseInt(cmd.getOptionValue("limit"));
                } catch (NumberFormatException e) {
                    throw error("\"" + cmd.getOptionValue("limit") + "\"" + " is not a number in option: limit");
                }
                if (limit < 0) {
                    throw error("\"" + limit + "\"" + " is not a positive number in option: limit");
                }
            }
            return new JduOptions(cmd.hasOption("L"), depth, limit, ComparatorType.SIZE_COMPARATOR, absolutePath);
        } catch (ParseException e) {
            throw new JduInvalidArgumentsException(e.getMessage(), e);
        }
    }

    @Contract("_ -> new")
    private static @NotNull JduInvalidArgumentsException error(@NotNull String message) {
        return new JduInvalidArgumentsException(message);
    }

}
