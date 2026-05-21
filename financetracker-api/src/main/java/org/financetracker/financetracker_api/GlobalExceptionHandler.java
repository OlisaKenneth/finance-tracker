package org.financetracker.financetracker_api;

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
     *
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
    @ExceptionHandler(MethodArgumentNotValidException.class) // <- This says: "when you see a MethodArgumentNotValidException thrown anywhere — run THIS method."
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // step 1: collect all error messages from failed validations
        // getBindingResult() gets all the validation failures visual:
                    /*BindingResult contains:
                        ├── field: "category", error: "Category cannot be empty"
                        └── field: "monthlyLimit", error: "Monthly limit must be greater than 0"*/

        // getFieldErrors() gets the specific field that failed visual:
                      /*[
                             FieldError { field: "category",     message: "Category cannot be empty" },
                             FieldError { field: "monthlyLimit", message: "Monthly limit must be greater than 0" }
                     ]*/

        // .stream() <- Converts the list into a stream — a pipeline you can process one item at a time

        // .map(error -> error.getDefaultMessage()) <- For each FieldError on the conveyor belt, extract just the message and throw away everything else

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

    /*
     * Catches wrong URL errors
     * Runs when: user visits a URL that doesn't exist
     * e.g. localhost:8080/api/wrongurl
     * Returns: 404 Not Found with a clear message
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            NoResourceFoundException ex) {

        // create a simple message map
        // Map<String, String> means: key is text label, value is text message
        // e.g. {"error": "The URL you requested does not exist"}
        Map<String, String> response = new HashMap<>();
        response.put("error", "The URL you requested does not exist");

        // 404 = "not found"
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /*
     * Catches database errors
     * Runs when: user tries to save duplicate data
     * e.g. two budgets with the same category
     * Returns: 409 Conflict with a clear message
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseErrors(
            DataIntegrityViolationException ex) {

        // create a simple message map
        // {"error": "This data already exists or violates a database rule"}
        Map<String, String> response = new HashMap<>();
        response.put("error", "This data already exists or violates a database rule");

        // 409 = "conflict — this already exists"
        // e.g. trying to create two budgets with the same category
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /*
     * Catches ALL other unexpected errors
     * This is the final safety net — catches anything we did not plan for
     * Runs when: something unexpected happens that we did not plan for
     * Returns: 500 Internal Server Error with a general message
     *
     * Exception.class means catch EVERYTHING
     * This must always be the LAST @ExceptionHandler
     * because Spring reads them top to bottom
     * and Exception.class would swallow all specific ones if placed first
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllOtherErrors(
            Exception ex) {

        // create a simple message map
        // {"error": "Something went wrong on the server"}
        Map<String, String> response = new HashMap<>();
        response.put("error", "Something went wrong on the server");

        // 500 = "server error — something broke on our end, not the user's fault"
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}