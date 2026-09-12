package com.rd.nse.service;

import com.rd.nse.entity.Symbol;
import com.rd.nse.repository.SymbolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SymbolService {

    @Autowired
    private SymbolRepository symbolRepository;

    public List<Symbol> getAllSymbols() {
        return symbolRepository.findAll();
    }

    public Symbol addSymbol(Symbol symbol) {
        return symbolRepository.save(symbol);
    }

    public Optional<Symbol> getBySymbol(String symbol) {
        return symbolRepository.findBySymbol(symbol);
    }

    public Optional<Symbol> getById(Long id) {
        return symbolRepository.findById(id);
    }

    public List<Symbol> addSymbols(List<Symbol> symbols) {
        return symbolRepository.saveAll(symbols);
    }

}

