package ru.nsu.fit.sckwo;

import org.apache.commons.cli.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.comparators.ComparatorType;
import ru.nsu.fit.sckwo.exception.JduException;
import ru.nsu.fit.sckwo.exception.JduInvalidArgumentsException;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Integer.parseInt;

public class JduOptionsParser {
    final static Options options;
    static final int DEFAULT_DEPTH = 128;
    static final int DEFAULT_LIMIT = 128;

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

            System.out.println(filePath);
            // CR: support regular files and symlinks as rootAbsolutePath
            Path absolutePath = Path.of(filePath);
            if (!Files.exists(absolutePath)) {
                throw error(absolutePath + " does not exist.");
            }
            if (!Files.isDirectory(absolutePath)) {
                throw error(absolutePath + " is not a directory.");
            }
            int depth = DEFAULT_DEPTH, limit = DEFAULT_LIMIT;
            if (cmd.hasOption("depth")) {
                depth = parseInt(cmd.getOptionValue("depth"));
                if (depth < 0) {
                    throw error("The specified parameter cannot be a negative number: depth");
                }
            }
            if (cmd.hasOption("limit")) {
                limit = parseInt(cmd.getOptionValue("limit"));
                if (limit < 0) {
                    throw error("The specified parameter cannot be a negative number: limit");
                }
            }
            return new JduOptions(cmd.hasOption("L"), depth, limit, ComparatorType.SIZE_COMPARATOR, absolutePath);
        } catch (ParseException | JduException e) {
            throw new JduInvalidArgumentsException(e);
        }
    }

    @Contract("_ -> new")
    private static @NotNull JduInvalidArgumentsException error(@NotNull String message) {
        return new JduInvalidArgumentsException(message);
    }

}
