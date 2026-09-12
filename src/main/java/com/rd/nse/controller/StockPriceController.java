package com.rd.nse.controller;

import com.rd.nse.entity.StockPrice1Min;
import com.rd.nse.entity.StockPrice1Day;
import com.rd.nse.service.StockPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-prices")
public class StockPriceController {

    @Autowired
    private StockPriceService stockPriceService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @GetMapping("/{timeframe}")
    public ResponseEntity<?> getPricesByTimeframe(
            @PathVariable String timeframe,
            @RequestParam String symbol,
            @RequestParam String start,
            @RequestParam String end) {

        LocalDateTime startTime;
        LocalDateTime endTime;

        try {
            startTime = LocalDateTime.parse(start, DATE_TIME_FORMATTER);
            endTime = LocalDateTime.parse(end, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid datetime format. Please use ISO-8601 format (yyyy-MM-dd'T'HH:mm:ss)");
        }


        return switch (timeframe.toLowerCase()) {
            case "1min" -> ResponseEntity.ok(
                    stockPriceService.get1MinPrices(symbol, startTime, endTime));
            case "1day" -> ResponseEntity.ok(
                    stockPriceService.get1DayPrices(symbol, startTime, endTime));
            default -> ResponseEntity.badRequest()
                    .body("Unsupported timeframe: " + timeframe);
        };
    }

    @GetMapping("/{timeframe}/latest")
    public ResponseEntity<?> getLatestPrice(
            @PathVariable String timeframe,
            @RequestParam String symbol) {

        return switch (timeframe.toLowerCase()) {
            case "1min" -> ResponseEntity.ok(
                    stockPriceService.getLatest1MinPrice(symbol));
            case "1day" -> ResponseEntity.ok(
                    stockPriceService.getLatest1DayPrice(symbol));
            default -> ResponseEntity.badRequest()
                    .body("Unsupported timeframe: " + timeframe);
        };
    }

    @PostMapping("/{timeframe}/batch")
    public ResponseEntity<?> saveBatch(
            @PathVariable String timeframe,
            @RequestBody List<?> prices) {

        return switch (timeframe.toLowerCase()) {
            case "1min" -> ResponseEntity.ok(
                    stockPriceService.saveAll1MinPrices((List<StockPrice1Min>) prices));
            case "1day" -> ResponseEntity.ok(
                    stockPriceService.saveAll1DayPrices((List<StockPrice1Day>) prices));
            default -> ResponseEntity.badRequest()
                    .body("Unsupported timeframe: " + timeframe);
        };
    }
}