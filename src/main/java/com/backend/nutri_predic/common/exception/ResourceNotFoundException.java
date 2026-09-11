package com.backend.nutri_predic.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource) {
        super(resource + " no encontrado");
    }
}
