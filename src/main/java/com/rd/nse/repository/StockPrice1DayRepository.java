package com.rd.nse.repository;

import com.rd.nse.entity.StockPrice1Day;
import com.rd.nse.entity.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockPrice1DayRepository extends JpaRepository<StockPrice1Day, LocalDateTime> {
    List<StockPrice1Day> findBySymbol(Symbol symbol);

    List<StockPrice1Day> findAllBySymbolSymbolAndTimestampBetween(String symbol, LocalDateTime start, LocalDateTime end);

    Optional<Object> findTopBySymbolSymbolOrderByTimestampDesc(String symbol);
}