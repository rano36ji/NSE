package com.rd.nse.repository;

import com.rd.nse.entity.StockPrice1Min;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockPrice1MinRepository extends JpaRepository<StockPrice1Min, LocalDateTime> {
    List<StockPrice1Min> findAllBySymbolSymbolAndTimestampBetween(String symbol, LocalDateTime startTime, LocalDateTime endTime);
    Optional<StockPrice1Min> findTopBySymbolSymbolOrderByTimestampDesc(String symbol);

    // Custom queries can be added here
}