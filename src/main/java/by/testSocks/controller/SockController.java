package by.testSocks.controller;


import by.testSocks.entity.Sock;
import by.testSocks.service.SockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/socks")
public class SockController {

    private final SockService sockService;

    @PostMapping("/income")
    public ResponseEntity<Sock> registerSock(@RequestBody Sock sock) {
        if (sock.getColor() == null || sock.getCottonPercentage() <= 0 || sock.getQuantity() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(sockService.addIncome(sock));
    }

    @PostMapping("/outcome")
    public ResponseEntity<Sock> outcome(@RequestBody Sock sock) {
        return ResponseEntity.ok(sockService.addOutcome(sock));
    }

    @GetMapping
    public ResponseEntity<List<Sock>> getSocks(
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) Double cottonPercentage,
            @RequestParam(required = false) Double minCotton,
            @RequestParam(required = false) Double maxCotton) {
        return ResponseEntity.ok(sockService.getFilteredSocks(color, operator, cottonPercentage, minCotton, maxCotton));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sock> updateSock(@PathVariable Long id, @RequestBody Sock sock) {
        return ResponseEntity.ok(sockService.updateSock(id, sock));
    }

    @PostMapping("/batch")
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("The file must not be empty");
        }
        try {
            if (Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx")) {
                return sockService.handleExcelFile(file);
            } else {
                return ResponseEntity.badRequest().body("Unsupported file format");
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing file");
        }
    }
}
