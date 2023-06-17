package ru.nsu.fit.sckwo.core;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.dufile.DuFileType;

import java.nio.file.Path;
import java.util.Arrays;

public class DuFileHelper {
    @NotNull
    public static DuFileWithChildren dir(String name, DuFileWithChildren... children) {
        DuFileWithChildren dir = new DuFileWithChildren(Path.of(name), DuFileType.DIRECTORY);
        dir.getChildren().addAll(Arrays.stream(children).toList());
        dir.setActualCountOfChildren(children.length);
        dir.setSize(0);
        return dir;
    }

    @NotNull
    public static DuFileWithChildren file(@NotNull String name) {
        DuFileWithChildren file = new DuFileWithChildren(Path.of(name), DuFileType.REGULAR_FILE);
        file.setSize(0);
        return file;
    }

    @NotNull
    public static DuFileWithChildren symlink(@NotNull String symlinkName, @NotNull DuFileWithChildren targetFile) {
        DuFileWithChildren symlink = new DuFileWithChildren(Path.of(symlinkName), DuFileType.SYMLINK);
        symlink.setSize(0);
        symlink.getChildren().add(targetFile);
        symlink.setActualCountOfChildren(1);
        return symlink;
    }
}
