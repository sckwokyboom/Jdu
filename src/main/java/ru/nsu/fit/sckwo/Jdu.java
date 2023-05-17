package ru.nsu.fit.sckwo;

import ru.nsu.fit.sckwo.exception.JduRuntimeException;

import java.io.IOException;

public class Jdu {
    JduOptions options;


    public Jdu(JduOptions options) {
        this.options = options;
    }

    public void printFileTree() throws IOException {
        TreePrinter treePrinter = new TreePrinter(options);
        try {
            treePrinter.print(new DuFile(options.rootAbsolutePath()), 0);
        } catch (RuntimeException e) {
            throw new JduRuntimeException(e);
        }
    }

}
