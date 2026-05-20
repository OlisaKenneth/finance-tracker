package org.financetracker.financetracker_api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
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
     * This method handles validation errors specifically
     * It runs whenever @Valid rejects something in ANY controller
     *
     * MethodArgumentNotValidException is the error Spring throws
     * when @Valid finds a problem
     *
     * Steps:
     * 1. Collect all the error messages from the failed validations
     * 2. Put them in a list
     * 3. Return that list as JSON with status 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // step 1: collect all error messages from failed validations
        // getBindingResult() gets all the validation failures
        // getFieldErrors() gets the specific field that failed
        // getDefaultMessage() gets the message we wrote e.g. "Category cannot be empty"
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .toList();

        // step 2: put the errors in a map so it comes back as JSON
        // {"errors": ["Category cannot be empty"]}
        Map<String, List<String>> response = new HashMap<>();
        response.put("errors", errors);

        // step 3: return 400 Bad Request with the error messages
        // 400 means "you sent something wrong"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}