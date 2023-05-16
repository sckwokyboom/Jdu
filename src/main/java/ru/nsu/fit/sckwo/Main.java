package ru.nsu.fit.sckwo;

import ru.nsu.fit.sckwo.exception.JduException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            JduOptionsParser jduOptionsParser = new JduOptionsParser();
            JduOptions jduOptions = jduOptionsParser.parseOptions(args);
            Jdu jdu = new Jdu(jduOptions);
            jdu.printFileTree();

        } catch (JduException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}