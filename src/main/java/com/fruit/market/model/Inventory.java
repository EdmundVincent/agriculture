package com.fruit.market.model;

import java.time.LocalDateTime;

public record Inventory(
    String fruitType,
    double quantity,
    double minThreshold,
    double maxCapacity,
    LocalDateTime lastUpdated
) {}
