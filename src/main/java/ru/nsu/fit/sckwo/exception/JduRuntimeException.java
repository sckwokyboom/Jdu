package ru.nsu.fit.sckwo.exception;

import org.jetbrains.annotations.NotNull;

public class JduRuntimeException extends JduException {
    public JduRuntimeException(@NotNull String message) {
        super("jdu: " + message);
    }

    public JduRuntimeException(@NotNull Throwable t) {
        super(t);
    }
}