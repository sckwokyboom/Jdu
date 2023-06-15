package ru.nsu.fit.sckwo;

import ru.nsu.fit.sckwo.dufile.DuFile;

public interface FileVisitor {

    void visitFile(DuFile curFile, int depthLevel);
}
