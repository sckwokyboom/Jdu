package ru.nsu.fit.sckwo;

import ru.nsu.fit.sckwo.exception.JduException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            JduOptionsParser jduOptionsParser = new JduOptionsParser();
            JduOptions jduOptions = jduOptionsParser.parseOptions(args);
            Jdu jdu = new Jdu(jduOptions, System.out);
            jdu.printFileTree();
        } catch (JduException | IOException e) {
            System.err.println(e.getMessage());
        }

    }
}