package com.eventnode.eventnodeapi.dtos;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Validador compartido para tests de DTOs (sin levantar todo el contexto Spring).
 */
final class DtoValidatorHolder {

    static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private DtoValidatorHolder() {
    }
}
