package com.fruit.market.service;

import com.fruit.market.model.SaleRecord;
import com.fruit.market.model.Inventory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class SalesService {

    private final List<SaleRecord> salesData = new ArrayList<>();
    private final Map<String, Inventory> inventoryData = new HashMap<>();
    private static final String[] FRUIT_TYPES = {"苹果", "香蕉", "橙子", "葡萄", "西瓜", "芒果", "菠萝", "草莓"};

    @PostConstruct
    public void initData() {
        // Generate mock data for the last 30 days
        generateMoreData(500);
        // Initialize inventory
        initializeInventory();
    }

    private void initializeInventory() {
        inventoryData.put("苹果", new Inventory("苹果", 500, 100, 1000, LocalDateTime.now()));
        inventoryData.put("香蕉", new Inventory("香蕉", 300, 80, 600, LocalDateTime.now()));
        inventoryData.put("橙子", new Inventory("橙子", 400, 100, 800, LocalDateTime.now()));
        inventoryData.put("葡萄", new Inventory("葡萄", 250, 50, 500, LocalDateTime.now()));
        inventoryData.put("西瓜", new Inventory("西瓜", 150, 30, 300, LocalDateTime.now()));
        inventoryData.put("芒果", new Inventory("芒果", 200, 50, 400, LocalDateTime.now()));
        inventoryData.put("菠萝", new Inventory("菠萝", 180, 40, 360, LocalDateTime.now()));
        inventoryData.put("草莓", new Inventory("草莓", 100, 20, 200, LocalDateTime.now()));
    }

    public void generateMoreData(int count) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(30);

        for (int i = 0; i < count; i++) {
            salesData.add(generateRandomSale(startTime, endTime));
        }
        
        // Sort by time descending
        salesData.sort((a, b) -> b.saleTime().compareTo(a.saleTime()));
    }

    public void addSale(String fruitType, double quantity, double pricePerKg) {
        String id = UUID.randomUUID().toString();
        double totalAmount = Math.round(quantity * pricePerKg * 100.0) / 100.0;
        LocalDateTime saleTime = LocalDateTime.now();
        
        SaleRecord record = new SaleRecord(id, fruitType, quantity, pricePerKg, totalAmount, saleTime);
        salesData.add(0, record); // Add to the beginning
    }

    private SaleRecord generateRandomSale(LocalDateTime start, LocalDateTime end) {
        String id = UUID.randomUUID().toString();
        String fruitType = FRUIT_TYPES[ThreadLocalRandom.current().nextInt(FRUIT_TYPES.length)];
        double quantity = Math.round(ThreadLocalRandom.current().nextDouble(1.0, 20.0) * 100.0) / 100.0;
        double pricePerKg = getBasePrice(fruitType) + (ThreadLocalRandom.current().nextDouble(-0.5, 0.5));
        double totalAmount = Math.round(quantity * pricePerKg * 100.0) / 100.0;
        
        long startEpoch = start.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = end.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        LocalDateTime saleTime = LocalDateTime.ofEpochSecond(randomEpoch, 0, java.time.ZoneOffset.UTC);

        return new SaleRecord(id, fruitType, quantity, pricePerKg, totalAmount, saleTime);
    }

    private double getBasePrice(String fruitType) {
        switch (fruitType) {
            case "苹果": return 5.0;
            case "香蕉": return 3.0;
            case "橙子": return 4.0;
            case "葡萄": return 8.0;
            case "西瓜": return 2.0;
            case "芒果": return 10.0;
            case "菠萝": return 6.0;
            case "草莓": return 15.0;
            default: return 5.0;
        }
    }

    public List<SaleRecord> getAllSales() {
        return salesData;
    }

    public Map<String, Double> getSalesByFruitType() {
        return salesData.stream()
                .collect(Collectors.groupingBy(
                        SaleRecord::fruitType,
                        Collectors.summingDouble(SaleRecord::totalAmount)
                ));
    }

    public Map<Integer, Double> getSalesByHour() {
        return salesData.stream()
                .collect(Collectors.groupingBy(
                        record -> record.saleTime().getHour(),
                        Collectors.summingDouble(SaleRecord::totalAmount)
                ));
    }
    
    public Map<String, String> getRestockSuggestions() {
        Map<String, Double> sales = getSalesByFruitType();
        Map<String, String> suggestions = new HashMap<>();
        
        double averageSales = sales.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        sales.forEach((fruit, total) -> {
            if (total > averageSales * 1.2) {
                suggestions.put(fruit, "高需求：建议增加库存");
            } else if (total < averageSales * 0.8) {
                suggestions.put(fruit, "低需求：建议促销");
            } else {
                suggestions.put(fruit, "需求平稳：维持库存");
            }
        });
        return suggestions;
    }

    // ===== Inventory Management =====
    public List<Inventory> getAllInventory() {
        return new ArrayList<>(inventoryData.values());
    }

    public void updateInventory(String fruitType, double quantity) {
        Inventory current = inventoryData.getOrDefault(fruitType, 
            new Inventory(fruitType, quantity, 50, 1000, LocalDateTime.now()));
        inventoryData.put(fruitType, new Inventory(fruitType, quantity, 
            current.minThreshold(), current.maxCapacity(), LocalDateTime.now()));
    }

    // ===== Sales Statistics =====
    public Map<String, Object> getDailySalesStats() {
        Map<String, Double> dailySales = salesData.stream()
                .collect(Collectors.groupingBy(
                        record -> record.saleTime().toLocalDate().toString(),
                        Collectors.summingDouble(SaleRecord::totalAmount)
                ));
        return new TreeMap<>(dailySales);
    }

    public Map<String, Object> getMonthlySalesStats() {
        Map<String, Double> monthlySales = salesData.stream()
                .collect(Collectors.groupingBy(
                        record -> record.saleTime().getYear() + "-" + 
                                String.format("%02d", record.saleTime().getMonthValue()),
                        Collectors.summingDouble(SaleRecord::totalAmount)
                ));
        return new TreeMap<>(monthlySales);
    }

    public Map<String, Object> getWeeklySalesStats() {
        Map<String, Object> weeklySales = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < 4; i++) {
            LocalDateTime weekStart = now.minusWeeks(i).with(java.time.temporal.ChronoField.DAY_OF_WEEK, 1);
            LocalDateTime weekEnd = weekStart.plusDays(6);
            double weekTotal = salesData.stream()
                    .filter(r -> !r.saleTime().isBefore(weekStart) && 
                               !r.saleTime().isAfter(weekEnd))
                    .mapToDouble(SaleRecord::totalAmount)
                    .sum();
            String weekLabel = "周" + (weekStart.getMonthValue()) + "-" + weekStart.getDayOfMonth();
            weeklySales.put(weekLabel, weekTotal);
        }
        
        return weeklySales;
    }

    // ===== Profit Analysis =====
    public Map<String, Object> getProfitByFruitType() {
        Map<String, Object> profitData = new HashMap<>();
        Map<String, List<SaleRecord>> groupedByFruit = salesData.stream()
                .collect(Collectors.groupingBy(SaleRecord::fruitType));
        
        groupedByFruit.forEach((fruitType, records) -> {
            double totalRevenue = records.stream().mapToDouble(SaleRecord::totalAmount).sum();
            long count = records.size();
            double avgPrice = records.stream().mapToDouble(SaleRecord::pricePerKg).average().orElse(0.0);
            double avgQuantity = records.stream().mapToDouble(SaleRecord::quantity).average().orElse(0.0);
            
            // Assume cost is 60% of revenue
            double estimatedCost = totalRevenue * 0.6;
            double estimatedProfit = totalRevenue - estimatedCost;
            double profitMargin = totalRevenue > 0 ? (estimatedProfit / totalRevenue * 100) : 0;
            
            Map<String, Object> fruitProfit = new HashMap<>();
            fruitProfit.put("revenue", Math.round(totalRevenue * 100.0) / 100.0);
            fruitProfit.put("estimatedCost", Math.round(estimatedCost * 100.0) / 100.0);
            fruitProfit.put("estimatedProfit", Math.round(estimatedProfit * 100.0) / 100.0);
            fruitProfit.put("profitMargin", Math.round(profitMargin * 100.0) / 100.0);
            fruitProfit.put("salesCount", count);
            fruitProfit.put("avgPrice", Math.round(avgPrice * 100.0) / 100.0);
            fruitProfit.put("avgQuantity", Math.round(avgQuantity * 100.0) / 100.0);
            
            profitData.put(fruitType, fruitProfit);
        });
        
        return profitData;
    }

    public Map<String, Object> getOverallProfitStats() {
        double totalRevenue = salesData.stream().mapToDouble(SaleRecord::totalAmount).sum();
        double estimatedCost = totalRevenue * 0.6;
        double estimatedProfit = totalRevenue - estimatedCost;
        long totalTransactions = salesData.size();
        double avgTransactionValue = totalTransactions > 0 ? totalRevenue / totalTransactions : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        stats.put("estimatedCost", Math.round(estimatedCost * 100.0) / 100.0);
        stats.put("estimatedProfit", Math.round(estimatedProfit * 100.0) / 100.0);
        stats.put("profitMargin", Math.round((estimatedProfit / totalRevenue * 100) * 100.0) / 100.0);
        stats.put("totalTransactions", totalTransactions);
        stats.put("avgTransactionValue", Math.round(avgTransactionValue * 100.0) / 100.0);
        
        return stats;
    }
}
