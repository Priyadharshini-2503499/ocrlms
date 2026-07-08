package com.genc.omnichannel.returns.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.genc.omnichannel.returns.dto.InitiateReturnRequest;
import com.genc.omnichannel.returns.dto.ReturnResponse;
import com.genc.omnichannel.returns.service.ReturnsService;

@RestController
@RequestMapping("/api/returns")
public class ReturnsRestController {

    private final ReturnsService returnsService;

    public ReturnsRestController(ReturnsService returnsService) {
        this.returnsService = returnsService;
    }

    @GetMapping
    public ResponseEntity<List<ReturnResponse>> getAllReturns() {
        List<ReturnResponse> returns = returnsService.getAllReturns();
        return ResponseEntity.ok(returns);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReturnById(@PathVariable Long id) {
        try {
            ReturnResponse returnResponse = returnsService.getReturnRequest(id);
            return ResponseEntity.ok(returnResponse);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> initiateReturn(@RequestBody InitiateReturnRequest returnRequest) {
        try {
            ReturnResponse saved = returnsService.initiateReturn(returnRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveReturn(@PathVariable Long id) {
        try {
            ReturnResponse approved = returnsService.approveReturn(id);
            return ResponseEntity.ok(approved);
        } catch (IllegalStateException | NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectReturn(@PathVariable Long id) {
        try {
            ReturnResponse rejected = returnsService.rejectReturn(id);
            return ResponseEntity.ok(rejected);
        } catch (IllegalStateException | NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<?> processRefund(@PathVariable Long id) {
        try {
            ReturnResponse refunded = returnsService.processRefund(id);
            return ResponseEntity.ok(refunded);
        } catch (IllegalStateException | NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}

