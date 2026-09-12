package com.rd.nse.service;

import com.rd.nse.entity.StockPrice1Day;
import com.rd.nse.entity.Symbol;
import com.rd.nse.repository.StockPrice1DayRepository;
import com.rd.nse.repository.SymbolRepository;
import com.rd.nse.util.FinanceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StockDataRefreshService {
    private static final Logger logger = LoggerFactory.getLogger(StockDataRefreshService.class);

    @Autowired
    private SymbolRepository symbolRepository;

    @Autowired
    private StockPrice1DayRepository stockPrice1DayRepository;

    /**
     * Refreshes data for all symbols
     * @return Number of records updated
     */
    public int refreshAllData() {
        List<Symbol> symbols = symbolRepository.findAll();
        int totalUpdated = 0;

        for (Symbol symbol : symbols) {
            try {
                totalUpdated += refreshSymbolData(symbol.getSymbol());
                // Add small delay to avoid rate limiting
                Thread.sleep(100);
            } catch (Exception e) {
                logger.error("Error refreshing data for symbol {}: {}", symbol.getSymbol(), e.getMessage());
            }
        }
        return totalUpdated;
    }

    /**
     * Refreshes data for a specific symbol
     * @param symbolName The symbol to refresh
     * @return Number of records updated
     */
    public int refreshSymbolData(String symbolName) {
        Symbol symbol = symbolRepository.findBySymbol(symbolName)
                .orElseThrow(() -> new RuntimeException("Symbol not found: " + symbolName));

        // Get existing timestamps to avoid duplicates
        Set<LocalDateTime> existingTimestamps = stockPrice1DayRepository
                .findBySymbol(symbol)
                .stream()
                .map(StockPrice1Day::getTimestamp)
                .collect(Collectors.toSet());

        // Fetch data from Yahoo Finance
        List<Map<String, Object>> historicalData = FinanceUtility
                .fetchHistoricalData(symbolName, "365d", "1d");

        List<StockPrice1Day> newPrices = new ArrayList<>();

        for (Map<String, Object> dataPoint : historicalData) {
            LocalDateTime timestamp = (LocalDateTime) dataPoint.get("timestamp");

            // Skip if we already have this timestamp
            if (existingTimestamps.contains(timestamp)) {
                continue;
            }

            StockPrice1Day price = new StockPrice1Day();
            price.setSymbol(symbol);
            price.setTimestamp(timestamp);
            price.setOpen(parseDouble(dataPoint.get("open")));
            price.setHigh(parseDouble(dataPoint.get("high")));
            price.setLow(parseDouble(dataPoint.get("low")));
            price.setClose(parseDouble(dataPoint.get("close")));
            price.setVolume(parseLong(dataPoint.get("volume")));

            newPrices.add(price);
        }

        if (!newPrices.isEmpty()) {
            stockPrice1DayRepository.saveAll(newPrices);
        }

        return newPrices.size();
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;
        String s = value.toString().replace(",", "");
        return Double.parseDouble(s);
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        String cleaned = value.toString().replace(",", "");
        // If there's a decimal, parse as Double first and convert to Long
        double doubleValue = Double.parseDouble(cleaned);
        return (long) doubleValue;
    }


}
