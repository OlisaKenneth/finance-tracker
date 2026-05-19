package org.financetracker.financetracker_api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private TransactionService transactionService;


    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @GetMapping // handles GET requests — used for READING data
    public List<Transaction> getAllTransactions(){
        return transactionService.getAllTransaction();
    }

    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction){

        return transactionService.createTransaction(transaction.getAmount(),transaction.getCategory(), transaction.getDescription());
    }
//value,category,description,today.toString()

}
