package com.rd.nse.controller;

import com.rd.nse.service.StockDataRefreshService;
import com.rd.nse.service.StockPriceService;
import com.rd.nse.util.FinanceUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FinanceController {

    @Autowired
    private StockDataRefreshService stockDataRefreshService;

    @Autowired
    private StockPriceService stockPriceService;

    @GetMapping("/historical")
    public List<Map<String, Object>> getHistoricalData(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1mo") String range,
            @RequestParam(defaultValue = "1d") String interval) {
        return FinanceUtility.fetchHistoricalData(symbol, range, interval);
    }

    @PostMapping("/refresh/all")
    public ResponseEntity<Map<String, Object>> refreshAllData() {
        Map<String, Object> response = new HashMap<>();
        try {
            int updatedCount = stockDataRefreshService.refreshAllData();
            response.put("status", "success");
            response.put("recordsUpdated", updatedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/refresh/symbol/{symbol}")
    public ResponseEntity<Map<String, Object>> refreshSymbolData(@PathVariable String symbol) {
        Map<String, Object> response = new HashMap<>();
        try {
            int updatedCount = stockDataRefreshService.refreshSymbolData(symbol);
            response.put("status", "success");
            response.put("symbol", symbol);
            response.put("recordsUpdated", updatedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/sideways")
    public ResponseEntity<List<Map<String, Object>>> getSidewaysDetection(
            @RequestParam String symbol,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "5") int windowDays,
            @RequestParam(defaultValue = "2.0") double thresholdPercent
    ) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        List<Map<String, Object>> periods = stockPriceService.detectSidewaysPeriods(symbol, startDate, endDate, windowDays, thresholdPercent);
        return ResponseEntity.ok(periods);
    }

    @GetMapping("/support-resistance")
    public ResponseEntity<?> getSupportAndResistance(
            @RequestParam String symbol,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "1d") String interval,
            @RequestParam(defaultValue = "0.5") double bandWidth,
            @RequestParam(defaultValue = "3") int minTouches
    ) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        Map<String, Object> result = stockPriceService.findSupportAndResistance(symbol, startDate,endDate , interval, bandWidth, minTouches);
        return ResponseEntity.ok(result);
    }
}
