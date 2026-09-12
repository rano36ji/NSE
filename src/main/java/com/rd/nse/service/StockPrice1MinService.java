package com.rd.nse.service;

import com.rd.nse.entity.StockPrice1Min;
import com.rd.nse.repository.StockPrice1MinRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

public class StockPrice1MinService {
    @Autowired
    private StockPrice1MinRepository repository;

    public List<StockPrice1Min> getStockPrices(String symbol, LocalDateTime startTime, LocalDateTime endTime) {
        // Custom query logic
        return repository.findAllBySymbolSymbolAndTimestampBetween(symbol, startTime, endTime);
    }
}
