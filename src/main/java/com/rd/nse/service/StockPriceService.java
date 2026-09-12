package com.rd.nse.service;

import com.rd.nse.entity.StockPrice1Day;
import com.rd.nse.entity.StockPrice1Min;
import com.rd.nse.repository.StockPrice1DayRepository;
import com.rd.nse.repository.StockPrice1MinRepository;
import com.rd.nse.util.FinanceUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for handling stock price operations and analytics.
 *
 * This class provides various methods to:
 *  - Retrieve 1-minute and 1-day price data for given symbols and time ranges
 *  - Get the most recent stock price records
 *  - Save bulk price data entries into the database
 *  - Detect sideways trading periods for stocks
 *  - Analyze price bands to find support and resistance levels based on historical data
 *
 * It acts as a central business logic layer for stock-related calculations and data retrieval,
 * utilizing repositories for 1-minute and 1-day stock price entities.
 *
 * Typical interactions involve the FinanceController requesting analytics or price information,
 * with data operations performed on StockPrice1Min and StockPrice1Day entities.
 */
@Service
public class StockPriceService {

    private static final Logger logger = LoggerFactory.getLogger(StockPriceService.class);

    @Autowired
    private StockPrice1MinRepository stockPrice1MinRepo;

    @Autowired
    private StockPrice1DayRepository stockPrice1DayRepo;

    /**
     * Retrieves 1-minute interval stock prices for a given symbol within a specified time range.
     *
     * @param symbol the stock symbol
     * @param start  the start timestamp (inclusive)
     * @param end    the end timestamp (inclusive)
     * @return list of StockPrice1Min objects matching the criteria
     */
    public List<StockPrice1Min> get1MinPrices(String symbol, LocalDateTime start, LocalDateTime end) {
        logger.info("Fetching 1-minute prices for symbol: {}, from {} to {}", symbol, start, end);
        List<StockPrice1Min> prices = stockPrice1MinRepo.findAllBySymbolSymbolAndTimestampBetween(symbol, start, end);
        logger.debug("Found {} 1-minute price records", prices.size());
        return prices;
    }

    /**
     * Retrieves 1-day interval stock prices for a given symbol within a specified time range.
     *
     * @param symbol the stock symbol
     * @param start  the start timestamp (inclusive)
     * @param end    the end timestamp (inclusive)
     * @return list of StockPrice1Day objects matching the criteria
     */
    public List<StockPrice1Day> get1DayPrices(String symbol, LocalDateTime start, LocalDateTime end) {
        logger.info("Fetching 1-day prices for symbol: {}, from {} to {}", symbol, start, end);
        List<StockPrice1Day> prices = stockPrice1DayRepo.findAllBySymbolSymbolAndTimestampBetween(symbol, start, end);
        logger.debug("Found {} 1-day price records", prices.size());
        return prices;
    }

    /**
     * Retrieves the latest 1-minute stock price record for a given symbol.
     *
     * @param symbol the stock symbol
     * @return the most recent StockPrice1Min object
     * @throws RuntimeException if no data is found for the symbol
     */
    public StockPrice1Min getLatest1MinPrice(String symbol) {
        logger.info("Fetching latest 1-minute price for symbol: {}", symbol);
        return stockPrice1MinRepo.findTopBySymbolSymbolOrderByTimestampDesc(symbol)
                .orElseThrow(() -> {
                    logger.error("No 1-minute price data found for symbol: {}", symbol);
                    return new RuntimeException("No 1-minute price data found for symbol: " + symbol);
                });
    }

    /**
     * Retrieves the latest 1-day stock price record for a given symbol.
     *
     * @param symbol the stock symbol
     * @return the most recent StockPrice1Day object
     * @throws RuntimeException if no data is found for the symbol
     */
    public StockPrice1Day getLatest1DayPrice(String symbol) {
        logger.info("Fetching latest 1-day price for symbol: {}", symbol);
        return (StockPrice1Day)stockPrice1DayRepo.findTopBySymbolSymbolOrderByTimestampDesc(symbol)
                .orElseThrow(() -> {
                    logger.error("No daily price data found for symbol: {}", symbol);
                    return new RuntimeException("No daily price data found for symbol: " + symbol);
                });
    }

    /**
     * Saves a list of 1-minute stock price records to the database.
     *
     * @param prices list of StockPrice1Min objects to save
     * @return list of saved StockPrice1Min objects
     */
    public List<StockPrice1Min> saveAll1MinPrices(List<StockPrice1Min> prices) {
        logger.info("Saving {} 1-minute price records", prices.size());
        List<StockPrice1Min> saved = stockPrice1MinRepo.saveAll(prices);
        logger.debug("Saved {} 1-minute price records", saved.size());
        return saved;
    }

    /**
     * Saves a list of 1-day stock price records to the database.
     *
     * @param prices list of StockPrice1Day objects to save
     * @return list of saved StockPrice1Day objects
     */
    public List<StockPrice1Day> saveAll1DayPrices(List<StockPrice1Day> prices) {
        logger.info("Saving {} 1-day price records", prices.size());
        List<StockPrice1Day> saved = stockPrice1DayRepo.saveAll(prices);
        logger.debug("Saved {} 1-day price records", saved.size());
        return saved;
    }

    /**
     * Detects sideways trading periods for a stock symbol within a date range.
     * A sideways period is defined as a window of days where the price change percentage
     * is less than or equal to the specified threshold.
     *
     * @param symbol           the stock symbol
     * @param start            start timestamp (inclusive)
     * @param end              end timestamp (inclusive)
     * @param windowDays       number of days in the sliding window
     * @param thresholdPercent maximum allowed percent change to consider sideways
     * @return list of maps containing period details: start, end, min, max, percentChange
     */
    public List<Map<String, Object>> detectSidewaysPeriods(String symbol, LocalDateTime start, LocalDateTime end, int windowDays, double thresholdPercent) {
        logger.info("Detecting sideways periods for symbol: {}, windowDays: {}, thresholdPercent: {}", symbol, windowDays, thresholdPercent);
        List<StockPrice1Day> prices = get1DayPrices(symbol, start, end);
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i <= prices.size() - windowDays; i++) {
            List<StockPrice1Day> window = prices.subList(i, i + windowDays);
            double min = window.stream().mapToDouble(StockPrice1Day::getClose).min().orElse(Double.NaN);
            double max = window.stream().mapToDouble(StockPrice1Day::getClose).max().orElse(Double.NaN);
            double pctChange = ((max - min) / min) * 100.0;

            if (pctChange <= thresholdPercent) {
                Map<String, Object> period = new HashMap<>();
                period.put("start", window.get(0).getTimestamp());
                period.put("end", window.get(window.size() - 1).getTimestamp());
                period.put("min", min);
                period.put("max", max);
                period.put("percentChange", pctChange);
                result.add(period);
                logger.debug("Detected sideways period: start={}, end={}, pctChange={}", period.get("start"), period.get("end"), pctChange);
            }
        }
        logger.info("Detected {} sideways periods for symbol: {}", result.size(), symbol);
        return result;
    }

    /**
     * Inner class representing information about a price band (support or resistance).
     */
    public static class BandInfo {
        public double level;
        public int touches;
        public LocalDateTime firstTouch;
        public LocalDateTime lastTouch;
        public List<LocalDateTime> touchDates;

        /**
         * Constructs a BandInfo instance.
         *
         * @param level    the price level of the band
         * @param touchDates list of dates when the band was touched
         */
        public BandInfo(double level, List<LocalDateTime> touchDates) {
            this.level = level;
            this.touches = touchDates.size();
            this.firstTouch = touchDates.stream().min(LocalDateTime::compareTo).orElse(null);
            this.lastTouch = touchDates.stream().max(LocalDateTime::compareTo).orElse(null);
            this.touchDates = touchDates;
        }
    }

    /**
     * Finds support and resistance price bands for a stock symbol within a date range.
     * Bands are identified based on historical low and high prices, band width, and minimum touches.
     *
     * @param symbol     the stock symbol
     * @param start      start timestamp (inclusive)
     * @param end        end timestamp (inclusive)
     * @param interval   interval string (e.g., "1d") - currently unused
     * @param bandWidth  percentage width of the band
     * @param minTouches minimum number of touches to consider a valid band
     * @return map containing support and resistance bands with their details
     */
    public Map<String, Object> findSupportAndResistance(
            String symbol, LocalDateTime start, LocalDateTime end, String interval, double bandWidth, int minTouches) {
        logger.info("Finding support and resistance for symbol: {}, from {} to {}", symbol, start, end);
        List<StockPrice1Day> hist = get1DayPrices(symbol, start, end);

        if (hist.isEmpty()) {
            logger.debug("No historical data found for symbol: {}", symbol);
            return Collections.emptyMap();
        }

        Collections.reverse(hist);

        List<Double> lowValues = hist.stream().map(StockPrice1Day::getLow).collect(Collectors.toList());
        List<Double> highValues = hist.stream().map(StockPrice1Day::getHigh).collect(Collectors.toList());
        List<LocalDateTime> dates = hist.stream().map(StockPrice1Day::getTimestamp).collect(Collectors.toList());

        double minLow = Collections.min(lowValues);
        double maxHigh = Collections.max(highValues);
        double supportMargin = minLow * bandWidth / 100.0;
        double resistanceMargin = maxHigh * bandWidth / 100.0;

        List<BandInfo> supportBands = getBandsWithMinTouches(lowValues, dates, bandWidth, minTouches, true);
        List<BandInfo> resistanceBands = getBandsWithMinTouches(highValues, dates, bandWidth, minTouches, false);

        List<Map<String, Object>> supportList = mergeBandsWithinMargin(supportBands, supportMargin).stream()
                .filter(band -> band.get("touches") instanceof Number && ((Number) band.get("touches")).intValue() > minTouches)
                .collect(Collectors.toList());;
        List<Map<String, Object>> resistanceList = mergeBandsWithinMargin(resistanceBands, resistanceMargin).stream()
                .filter(band -> band.get("touches") instanceof Number && ((Number) band.get("touches")).intValue() > minTouches)
                .collect(Collectors.toList());;

        Map<String, Object> res = new HashMap<>();
        if (!supportList.isEmpty()) {
            for (Map<String, Object> band : supportList) {
                logger.info("Support Band - Level: {}, Touches: {}, First Touch: {}, Last Touch: {}",
                        band.get("level"),
                        band.get("touches"),
                        band.get("firstTouch"),
                        band.get("lastTouch"));
            }
            res.put("support", supportList);
        }

        if (!resistanceList.isEmpty()) {
            for (Map<String, Object> band : resistanceList) {
                logger.info("Resistance Band - Level: {}, Touches: {}, First Touch: {}, Last Touch: {}",
                        band.get("level"),
                        band.get("touches"),
                        band.get("firstTouch"),
                        band.get("lastTouch"));
            }
            res.put("resistance", resistanceList);
        }
        logger.debug("Found support and resistance bands for symbol: {}", symbol);
        return res;
    }

    private List<Map<String, Object>> mergeBandsWithinMargin(List<BandInfo> bands, double margin) {
        logger.debug("Merging bands within margin: {}", margin);
        List<Map<String, Object>> mergedBands = new ArrayList<>();
        
        for (BandInfo band : bands) {
            boolean merged = false;
            
            for (Map<String, Object> mergedBand : mergedBands) {
                double level = (double) mergedBand.get("level");
                
                if (Math.abs(level - band.level) <= margin) {
                    mergedBand.put("touches", (int) mergedBand.get("touches") + band.touches);
                    
                    mergedBand.put("firstTouch", band.firstTouch.isBefore((LocalDateTime) mergedBand.get("firstTouch")) ? band.firstTouch : mergedBand.get("firstTouch"));
                    mergedBand.put("lastTouch", band.lastTouch.isAfter((LocalDateTime) mergedBand.get("lastTouch")) ? band.lastTouch : mergedBand.get("lastTouch"));
                    
                    List<Map<String, Object>> touchDetails = (List<Map<String, Object>>) mergedBand.getOrDefault("touchDetails", new ArrayList<>());
                    touchDetails.addAll(band.touchDates.stream()
                            .map(date -> {
                                Map<String, Object> touch = new HashMap<>();
                                touch.put("value", band.level);
                                touch.put("date", date);
                                return touch;
                            })
                            .collect(Collectors.toList()));
                    //get all touches and dates
                    //   mergedBand.put("touchDetails", touchDetails);
                    
                    merged = true;
                    break;
                }
            }
            
            if (!merged) {
                Map<String, Object> info = new HashMap<>();
                info.put("level", band.level);
                info.put("touches", band.touches);
                info.put("firstTouch", band.firstTouch);
                info.put("lastTouch", band.lastTouch);
                
                List<Map<String, Object>> touchDetails = band.touchDates.stream()
                        .map(date -> {
                            Map<String, Object> touch = new HashMap<>();
                            touch.put("value", band.level);
                            touch.put("date", date);
                            return touch;
                        })
                        .collect(Collectors.toList());
                //get all touches and dates
                //     info.put("touchDetails", touchDetails);
                
                mergedBands.add(info);
            }
            
            logger.info("Band - Level: {}, Touches: {}, First Touch: {}, Last Touch: {}", band.level, band.touches, band.firstTouch, band.lastTouch);
            for (LocalDateTime date : band.touchDates) {
                logger.info("Touch - Value: {}, Date: {}", band.level, date);
            }
        }
        
        logger.debug("Merged {} bands", mergedBands.size());
        return mergedBands;
    }

    /**
     * Helper method to find price bands with at least a minimum number of touches.
     *
     * @param values     list of price values (low or high)
     * @param dateList   corresponding list of dates for the values
     * @param bandWidth  percentage width of the band
     * @param minTouches minimum number of touches to consider a valid band
     * @param ascending  whether to sort bands in ascending order
     * @return list of BandInfo objects meeting the criteria
     */
    private List<BandInfo> getBandsWithMinTouches(
            List<Double> values, List<LocalDateTime> dateList, double bandWidth, int minTouches, boolean ascending) {
        logger.debug("Finding bands with minimum touches: {}", minTouches);
        Map<Double, List<LocalDateTime>> bandTouches = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            double v = values.get(i);
            double band = Math.round(v / (v * bandWidth / 100.0)) * (v * bandWidth / 100.0);
            band = Math.round(band * 100.0) / 100.0;
            bandTouches.computeIfAbsent(band, k -> new ArrayList<>()).add(dateList.get(i));
     //       logger.info("Band Touch Value: {}, Date: {}", band, dateList.get(i));
        }

        // Sort by band value, then touches
        Comparator<Map.Entry<Double, List<LocalDateTime>>> comparator = Comparator
                .comparingInt((Map.Entry<Double, List<LocalDateTime>> e) -> e.getValue().size()).reversed()
                .thenComparing((Map.Entry<Double, List<LocalDateTime>> e) -> e.getKey(), ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());

        List<BandInfo> bands = bandTouches.entrySet().stream()
          //      .filter(e -> e.getValue().size() >= minTouches)
                .sorted(comparator)
                .map(e -> new BandInfo(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        logger.debug("Found {} bands with minimum touches", bands.size());
        return bands;
    }
}