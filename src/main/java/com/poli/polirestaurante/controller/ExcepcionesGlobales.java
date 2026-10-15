package com.poli.polirestaurante.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExcepcionesGlobales {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> conflicto(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Operación no permitida: dato duplicado o referenciado por otros registros.");
    }
}