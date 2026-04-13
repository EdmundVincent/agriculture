package com.fruit.market.controller;

import com.fruit.market.model.SaleRecord;
import com.fruit.market.model.Inventory;
import com.fruit.market.service.SalesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping
    public List<SaleRecord> getAllSales() {
        return salesService.getAllSales();
    }

    @GetMapping("/by-type")
    public Map<String, Double> getSalesByFruitType() {
        return salesService.getSalesByFruitType();
    }

    @GetMapping("/by-hour")
    public Map<Integer, Double> getSalesByHour() {
        return salesService.getSalesByHour();
    }
    
    @GetMapping("/suggestions")
    public Map<String, String> getSuggestions() {
        return salesService.getRestockSuggestions();
    }

    @PostMapping("/add")
    public void addSale(@RequestBody AddSaleRequest request) {
        salesService.addSale(request.fruitType(), request.quantity(), request.pricePerKg());
    }

    @PostMapping("/generate")
    public void generateData() {
        salesService.generateMoreData(50); // Generate 50 more records
    }

    public record AddSaleRequest(String fruitType, double quantity, double pricePerKg) {}

    // ===== Inventory Management Endpoints =====
    @GetMapping("/inventory")
    public List<Inventory> getAllInventory() {
        return salesService.getAllInventory();
    }

    @PostMapping("/inventory/update")
    public void updateInventory(@RequestBody UpdateInventoryRequest request) {
        salesService.updateInventory(request.fruitType(), request.quantity());
    }

    public record UpdateInventoryRequest(String fruitType, double quantity) {}

    // ===== Sales Statistics Endpoints =====
    @GetMapping("/stats/daily")
    public Map<String, Object> getDailySalesStats() {
        return salesService.getDailySalesStats();
    }

    @GetMapping("/stats/weekly")
    public Map<String, Object> getWeeklySalesStats() {
        return salesService.getWeeklySalesStats();
    }

    @GetMapping("/stats/monthly")
    public Map<String, Object> getMonthlySalesStats() {
        return salesService.getMonthlySalesStats();
    }

    // ===== Profit Analysis Endpoints =====
    @GetMapping("/profit/by-type")
    public Map<String, Object> getProfitByFruitType() {
        return salesService.getProfitByFruitType();
    }

    @GetMapping("/profit/overall")
    public Map<String, Object> getOverallProfitStats() {
        return salesService.getOverallProfitStats();
    }
}
