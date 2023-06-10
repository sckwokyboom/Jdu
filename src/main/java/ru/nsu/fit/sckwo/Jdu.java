package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.exception.JduInvalidArgumentsException;
import ru.nsu.fit.sckwo.exception.JduRuntimeException;

import java.io.IOException;
import java.io.PrintStream;

public class Jdu {
    private final JduOptions options;
    private final PrintStream printStream;


    public Jdu(JduOptions options, @NotNull PrintStream printStream) {
        this.options = options;
        this.printStream = printStream;
    }

    public void printFileTree() throws IOException, JduInvalidArgumentsException {
        TreePrinter treePrinter = new TreePrinter(options, printStream);
        try {
            treePrinter.print(new DuFile(options.rootAbsolutePath()));
        } catch (RuntimeException e) {
            throw new JduRuntimeException(e);
        }
    }

}
