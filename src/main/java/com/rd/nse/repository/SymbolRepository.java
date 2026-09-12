package com.rd.nse.repository;

import com.rd.nse.entity.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SymbolRepository extends JpaRepository<Symbol, Long> {
    Optional<Symbol> findBySymbol(String symbol);

    List<Symbol> findAll();

    Symbol save(Symbol symbol);
}

