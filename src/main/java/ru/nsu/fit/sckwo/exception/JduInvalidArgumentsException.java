package ru.nsu.fit.sckwo.exception;

import org.jetbrains.annotations.NotNull;

public class JduInvalidArgumentsException extends JduException {
    public JduInvalidArgumentsException(@NotNull String message) {
        super(message);
    }

    public JduInvalidArgumentsException(@NotNull Throwable t) {
        super(t);
    }

    public JduInvalidArgumentsException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}