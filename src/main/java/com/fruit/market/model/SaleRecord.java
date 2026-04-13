package com.fruit.market.model;

import java.time.LocalDateTime;

public record SaleRecord(
    String id,
    String fruitType,
    double quantity,
    double pricePerKg,
    double totalAmount,
    LocalDateTime saleTime
) {}
