package ru.nsu.fit.sckwo.core;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.dufile.DuFile;
import ru.nsu.fit.sckwo.dufile.DuFileType;

import java.nio.file.Path;
import java.util.Arrays;

public class DuFileHelper {
    @NotNull
    public static DuFile dir(String name, DuFile... children) {
        DuFile dir = new DuFile(Path.of(name), DuFileType.DIRECTORY);
        dir.getChildren().addAll(Arrays.stream(children).toList());
        dir.setSize(0);
        return dir;
    }

    @NotNull
    public static DuFile file(@NotNull String name) {
        DuFile file = new DuFile(Path.of(name), DuFileType.REGULAR_FILE);
        file.setSize(0);
        return file;
    }

    @NotNull
    public static DuFile symlink(@NotNull String symlinkName, @NotNull DuFile targetFile) {
        DuFile symlink = new DuFile(Path.of(symlinkName), DuFileType.SYMLINK);
        symlink.setSize(0);
        symlink.getChildren().add(targetFile);
        return symlink;
    }
}
