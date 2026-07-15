package org.financetracker.financetracker_api.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.*;

/*
 * This class catches ALL validation errors that happen across the entire app
 * Think of it like a safety net:
 * - User sends invalid data (empty category, negative amount)
 * - @Valid catches it and throws an error
 * - This class catches that error
 * - Returns a clean readable error message to the user
 *
 * Without this class Spring Boot returns a huge ugly error object
 * With this class the user gets a simple clean message like:
 * {"errors": ["Category cannot be empty", "Monthly limit must be greater than 0"]}
 *
 * @RestControllerAdvice means:
 * "watch ALL controllers and intercept any errors they throw"
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Catches validation errors from @Valid
     * Runs when: user sends empty category, negative amount etc.
     * Returns: 400 Bad Request with list of what went wrong
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .toList();

        Map<String, List<String>> response = new HashMap<>();
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /*
     * Catches wrong URL errors
     * Runs when: user visits a URL that doesn't exist
     * Returns: 404 Not Found with a clear message
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            NoResourceFoundException ex) {

        Map<String, String> response = new HashMap<>();
        response.put("error", "The URL you requested does not exist");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /*
     * Catches database errors
     * Runs when: user tries to save duplicate data
     * Returns: 409 Conflict with a clear message
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseErrors(
            DataIntegrityViolationException ex) {

        Map<String, String> response = new HashMap<>();
        response.put("error", "This data already exists or violates a database rule");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /*
     * Catches ALL other unexpected errors
     * This is the final safety net
     * ex.printStackTrace() prints the REAL error to Railway logs
     * so we can see exactly what went wrong
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllOtherErrors(
            Exception ex) {

        // prints the full real error to Railway logs — not hidden anymore
        ex.printStackTrace();

        Map<String, String> response = new HashMap<>();
        response.put("error", "Something went wrong on the server");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /*
     * Catches invalid login attempts (wrong email or password)
     * Runs when: UserService.login() throws IllegalArgumentException
     * Returns: 400 Bad Request with a clear message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidLogin(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}