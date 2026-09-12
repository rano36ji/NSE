package com.rd.nse.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceUtility {


    /**
     * Fetches historical stock data from Yahoo Finance API
     *
     * @param symbol   The stock symbol (e.g., "AAPL")
     * @param range    The time range (e.g., "1mo", "3mo", "1y")
     * @param interval The data interval (e.g., "1d", "1wk", "1mo")
     * @return List of maps containing historical data points
     */
    public static List<Map<String, Object>> fetchHistoricalData(String symbol, String range, String interval) {
        try {
            String apiUrl = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?range=%s&interval=%s",
                    symbol+".NS", range, interval);

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return parseYahooResponse(response.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching data: " + e.getMessage());
        }
    }

    /**
     * Parses the Yahoo Finance API response
     *
     * @param responseStr The JSON response string from Yahoo Finance API
     * @return List of maps containing parsed data points
     */
    private static List<Map<String, Object>> parseYahooResponse(String responseStr) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(responseStr);
            JSONObject chart = jsonResponse.getJSONObject("chart");
            JSONObject result0 = chart.getJSONArray("result").getJSONObject(0);

            JSONObject indicators = result0.getJSONObject("indicators");
            JSONObject quote = indicators.getJSONArray("quote").getJSONObject(0);

            long[] timestamps = result0.getJSONArray("timestamp").toList().stream()
                    .mapToLong(obj -> Long.parseLong(obj.toString()))
                    .toArray();
            LocalDateTime[] dateTimes = Arrays.stream(timestamps)
                    .mapToObj(ts -> Instant.ofEpochSecond(ts)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime())
                    .toArray(LocalDateTime[]::new);



            Object[] opens = quote.getJSONArray("open").toList().toArray();
            Object[] highs = quote.getJSONArray("high").toList().toArray();
            Object[] lows = quote.getJSONArray("low").toList().toArray();
            Object[] closes = quote.getJSONArray("close").toList().toArray();
            Object[] volumes = quote.getJSONArray("volume").toList().toArray();

            for (int i = 0; i < dateTimes.length; i++) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("timestamp", dateTimes[i]);
                dataPoint.put("open", String.format("%,.2f", Double.parseDouble(opens[i].toString())));
                dataPoint.put("high", String.format("%,.2f", Double.parseDouble(highs[i].toString())));
                dataPoint.put("low", String.format("%,.2f", Double.parseDouble(lows[i].toString())));
                dataPoint.put("close", String.format("%,.2f", Double.parseDouble(closes[i].toString())));
                dataPoint.put("volume", String.format("%,.2f", Double.parseDouble(volumes[i].toString())));
                result.add(dataPoint);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Yahoo Finance response: " + e.getMessage());
        }
        return result;
    }
}
