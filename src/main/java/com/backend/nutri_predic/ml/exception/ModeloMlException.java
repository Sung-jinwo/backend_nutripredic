package com.backend.nutri_predic.ml.exception;

public class ModeloMlException extends RuntimeException {
    public ModeloMlException(String message) {
        super(message);
    }

    public ModeloMlException(String message, Throwable cause) {
        super(message, cause);
    }
}
