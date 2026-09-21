package com.backend.nutri_predic.common.exception;

import com.backend.nutri_predic.common.dto.ApiErrorResponse;
import com.backend.nutri_predic.common.dto.ApiErrorResponse.FieldValidationError;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    org.springframework.http.ResponseEntity<ApiErrorResponse> responseStatus(
            org.springframework.web.server.ResponseStatusException exception, HttpServletRequest request) {
        var status = HttpStatus.valueOf(exception.getStatusCode().value());
        return org.springframework.http.ResponseEntity.status(status)
                .body(error(status, exception.getReason(), request));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse notFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse routeNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "La ruta solicitada no está disponible. Comprueba que el backend esté actualizado.", request);
    }

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse badRequest(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(DatosV5IncompletosException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse datosV5Incompletos(
            DatosV5IncompletosException exception, HttpServletRequest request) {
        var details = exception.getDatosFaltantes().stream()
                .map(value -> new FieldValidationError("datosFaltantes", value))
                .toList();
        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(), request.getRequestURI(), details);
    }

    @ExceptionHandler(DatosV6IncompletosException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse datosV6Incompletos(
            DatosV6IncompletosException exception, HttpServletRequest request) {
        var details = exception.getDatosFaltantes().stream()
                .map(value -> new FieldValidationError("datosFaltantes", value))
                .toList();
        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(), request.getRequestURI(), details);
    }

    @ExceptionHandler(ModeloMlException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    ApiErrorResponse modeloMl(ModeloMlException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler({
        ConflictException.class,
        DataIntegrityViolationException.class,
        ObjectOptimisticLockingFailureException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiErrorResponse conflict(Exception exception, HttpServletRequest request) {
        String message =
                exception instanceof ConflictException
                        ? exception.getMessage()
                        : "El recurso entra en conflicto con datos existentes";
        return error(HttpStatus.CONFLICT, message, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiErrorResponse accessDenied(HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "Acceso denegado", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiErrorResponse unauthorized(HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse invalid(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldValidationError> fieldErrors =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(
                                field ->
                                        new FieldValidationError(
                                                field.getField(), field.getDefaultMessage()))
                        .toList();
        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "La solicitud contiene campos inválidos",
                request.getRequestURI(),
                fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse invalidConstraint(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<FieldValidationError> fieldErrors =
                exception.getConstraintViolations().stream()
                        .map(
                                violation ->
                                        new FieldValidationError(
                                                violation.getPropertyPath().toString(),
                                                violation.getMessage()))
                        .toList();
        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "La solicitud contiene campos inválidos",
                request.getRequestURI(),
                fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse malformedJson(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "El cuerpo JSON es inválido", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    ApiErrorResponse unsupportedMediaType(HttpServletRequest request) {
        return error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El tipo de contenido de la solicitud no es compatible",
                request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    ApiErrorResponse unsupportedMethod(HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "El método HTTP no está permitido", request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ApiErrorResponse other(Exception exception, HttpServletRequest request) {
        log.error("Error no controlado al procesar {} {}", request.getMethod(), request.getRequestURI(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", request);
    }

    private ApiErrorResponse error(HttpStatus status, String message, HttpServletRequest request) {
        return ApiErrorResponse.of(
                status.value(), status.getReasonPhrase(), message, request.getRequestURI());
    }
}
