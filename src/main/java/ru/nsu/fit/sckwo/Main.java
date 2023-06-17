package ru.nsu.fit.sckwo;

import ru.nsu.fit.sckwo.exception.JduException;

public class Main {
    public static void main(String[] args) {
        try {
            JduOptionsParser jduOptionsParser = new JduOptionsParser();
            JduOptions jduOptions = jduOptionsParser.parseOptions(args);
            FileVisitor printer = new Printer(System.out, jduOptions.depth(), jduOptions.followSymlinks());
            TreeWalker treeWalker = new TreeWalker(jduOptions, printer);
            treeWalker.walk(jduOptions.rootAbsolutePath());
        } catch (JduException e) {
            System.err.println(e.getMessage());
        }
    }
}