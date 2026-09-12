package com.rd.nse.controller;

import com.rd.nse.entity.Symbol;
import com.rd.nse.service.SymbolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/symbols")
@Tag(name = "Symbol Controller", description = "APIs for managing stock symbols")
public class SymbolController {

    @Autowired
    private SymbolService symbolService;

    @Operation(summary = "Get all symbols", description = "Retrieves a list of all available stock symbols")
    @GetMapping
    public List<Symbol> getAll() {
        return symbolService.getAllSymbols();
    }

    @PostMapping
    public Symbol create(@RequestBody Symbol symbol) {
        return symbolService.addSymbol(symbol);
    }

    @PostMapping("/batch")
    public List<Symbol> createBatch(@RequestBody List<Symbol> symbols) {
        return symbolService.addSymbols(symbols);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Symbol> getById(@PathVariable Long id) {
        return symbolService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<Symbol> getBySymbol(@PathVariable String symbol) {
        return symbolService.getBySymbol(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
