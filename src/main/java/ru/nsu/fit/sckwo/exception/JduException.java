package ru.nsu.fit.sckwo.exception;

import org.jetbrains.annotations.NotNull;

public abstract class JduException extends RuntimeException {
    public JduException(@NotNull String message) {
        super("jdu: " + message);
    }

    public JduException(@NotNull Throwable t) {
        super(t);
    }

    public JduException(@NotNull String message, @NotNull Throwable cause) {
        super("jdu: " + message, cause);
    }
}
