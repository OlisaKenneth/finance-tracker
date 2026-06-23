package org.financetracker.financetracker_api.controller;

import jakarta.validation.Valid;
import org.financetracker.financetracker_api.model.Transaction;
import org.financetracker.financetracker_api.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.http.ResponseEntity;

/*
 * This class is our REQUEST HANDLER for transactions (the front door of the transactions API)
 * It receives HTTP requests from Postman or a browser
 * and decides what to do with them
 *
 * Think of it like a receptionist:
 * - Someone comes in asking for something (HTTP request)
 * - Receptionist listens and passes it to the right department (Service)
 * - Gets the result and hands it back to the person (HTTP response)
 */
@RestController // tells Spring Boot "this class handles HTTP requests and returns JSON"
@RequestMapping("/api/transactions") // all URLs in this class start with /api/transactions
public class TransactionController {

    // we need the service to handle the business logic
    // Spring Boot hands it to us automatically (dependency injection)
    private TransactionService transactionService;

    /*
     * Constructor — Spring Boot sees we need a TransactionService
     * and automatically passes one in (dependency injection)
     */
    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    /*
     * This method handles GET requests to /api/transactions
     * Returns ALL transactions as a JSON list
     *
     * Example response:
     * [{"id":1,"amount":50.0,"category":"Groceries","description":"milk","date":"2026-05-19"}]
     */
    @GetMapping // handles GET requests — used for READING data
    public List<Transaction> getAllTransactions(){
        return transactionService.getAllTransaction(); // ask the service to get all transactions
    }

    /*
     * This method handles POST requests to /api/transactions
     * Receives a new transaction, validates it, saves it and returns it
     *
     * @Valid runs the validation rules from Transaction.java
     * (@Positive on amount, @NotBlank on category and description)
     * BEFORE this method runs — if validation fails returns 400 Bad Request
     *
     * Example request body:
     * {"amount": 50.0, "category": "Groceries", "description": "milk and bread"}
     *
     * Example response:
     * {"id":1,"amount":50.0,"category":"Groceries","description":"milk and bread","date":"2026-05-19"}
     */
    @PostMapping // handles POST requests — used for CREATING data
    public Transaction createTransaction(@Valid @RequestBody Transaction transaction){
        // @Valid validates before this line runs
        // if amount is negative or category is blank → rejected before reaching here
        return transactionService.createTransaction(
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getDescription()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        boolean deleted = transactionService.deleteTransaction(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}