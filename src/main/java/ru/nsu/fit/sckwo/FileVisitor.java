package ru.nsu.fit.sckwo;

import ru.nsu.fit.sckwo.dufile.DuFile;

public interface FileVisitor {

    void printFileInfo(DuFile curFile);

    void visitFile(DuFile curFile, int depthLevel);
}
